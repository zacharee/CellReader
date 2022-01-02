/*
https://github.com/sahruday/Carousel
 */

package dev.zwander.cellreader.ui.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.MutatePriority

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.verticalScrollAxisRange
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Create and [remember] the [CarouselScrollState] based on the currently appropriate scroll
 * configuration to allow changing scroll position or observing scroll behavior.
 *
 * Learn how to control the state of [Modifier.verticalScroll] or [Modifier.horizontalScroll]:
 *
 * @param initial initial scroller position to start with
 *
 * @see Carousel
 */
@Composable
fun rememberCarouselScrollState(initial: Int = 0): CarouselScrollState {
    return rememberSaveable(saver = CarouselScrollState.Saver) {
        CarouselScrollState(initial = initial)
    }
}

/**
 * State of the scroll. Allows the developer to change the scroll position or get current state by
 * calling methods on this object. To be hosted and passed to [Modifier.verticalScroll] or
 * [Modifier.horizontalScroll]
 *
 * To create and automatically remember [CarouselScrollState] with default parameters use
 * [rememberCarouselScrollState].
 *
 * Learn how to control the state of [Modifier.verticalScroll] or [Modifier.horizontalScroll]:
 *
 * @param initial value of the scroll
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 *
 * [scrollableLength] param is added inorder to use it in [Carousel]
 *
 * @see Carousel
 *
 */
@Stable
class CarouselScrollState(initial: Int) : ScrollableState {

    /**
     * current scroll position value in pixels
     */
    var value: Int by mutableStateOf(initial, structuralEqualityPolicy())
        private set

    /**
     * maximum bound for [value], or [Int.MAX_VALUE] if still unknown
     */
    var maxValue: Int
        get() = _maxValueState.value
        internal set(newMax) {
            _maxValueState.value = newMax
            if (value > newMax) {
                value = newMax
            }
        }

    /**
     * total length that occupied by the children along scroll axis.
     */
    var scrollableLength: Int
        get() = _length.value
        internal set(length) {
            _length.value = length
        }

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * list is being dragged. If you want to know whether the fling (or smooth scroll) is in
     * progress, use [isScrollInProgress].
     */
    val interactionSource: InteractionSource get() = internalInteractionSource

    internal val internalInteractionSource: MutableInteractionSource = MutableInteractionSource()

    private var _maxValueState = mutableStateOf(Int.MAX_VALUE, structuralEqualityPolicy())

    private var _length = mutableStateOf(Int.MAX_VALUE, structuralEqualityPolicy())

    /**
     * We receive scroll events in floats but represent the scroll position in ints so we have to
     * manually accumulate the fractional part of the scroll to not completely ignore it.
     */
    private var accumulator: Float = 0f

    private val scrollableState = ScrollableState {
        val absolute = (value + it + accumulator)
        val newValue = absolute.coerceIn(0f, maxValue.toFloat())
        val changed = absolute != newValue
        val consumed = newValue - value
        val consumedInt = consumed.roundToInt()
        value += consumedInt
        accumulator = consumed - consumedInt

        // Avoid floating-point rounding error
        if (changed) consumed else it
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ): Unit = scrollableState.scroll(scrollPriority, block)

    override fun dispatchRawDelta(delta: Float): Float =
        scrollableState.dispatchRawDelta(delta)

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    /**
     * Scroll to position in pixels with animation.
     *
     * @param value target value in pixels to smooth scroll to, value will be coerced to
     * 0..maxPosition
     * @param animationSpec animation curve for smooth scroll animation
     */
    suspend fun animateScrollTo(
        value: Int,
        animationSpec: AnimationSpec<Float> = SpringSpec(),
    ) {
        this.animateScrollBy((value - this.value).toFloat(), animationSpec)
    }

    /**
     * Instantly jump to the given position in pixels.
     *
     * Cancels the currently running scroll, if any, and suspends until the cancellation is
     * complete.
     *
     * @see animateScrollTo for an animated version
     *
     * @param value number of pixels to scroll by
     * @return the amount of scroll consumed
     */
    suspend fun scrollTo(value: Int): Float = this.scrollBy((value - this.value).toFloat())

    companion object {
        /**
         * The default [Saver] implementation for [CarouselScrollState].
         */
        val Saver: Saver<CarouselScrollState, *> = Saver(
            save = { it.value },
            restore = { CarouselScrollState(it) }
        )
    }
}

/**
 * Modify element to allow to scroll vertically when height of the content is bigger than max
 * constraints allow.
 *
 * @sample androidx.compose.foundation.samples.VerticalScrollExample
 *
 * In order to use this modifier, you need to create and own [CarouselScrollState]
 * @see [rememberCarouselScrollState]
 *
 * @param state state of the scroll
 * @param enabled whether or not scrolling via touch input is enabled
 * @param flingBehavior logic describing fling behavior when drag has finished with velocity. If
 * `null`, default from [ScrollableDefaults.flingBehavior] will be used.
 * @param reverseScrolling reverse the direction of scrolling, when `true`, 0 [CarouselScrollState.value]
 * will mean bottom, when `false`, 0 [CarouselScrollState.value] will mean top
 */
fun Modifier.verticalScroll(
    state: CarouselScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
) = scroll(
    state = state,
    isScrollable = enabled,
    reverseScrolling = reverseScrolling,
    flingBehavior = flingBehavior,
    isVertical = true
)

/**
 * Modify element to allow to scroll horizontally when width of the content is bigger than max
 * constraints allow.
 *
 * In order to use this modifier, you need to create and own [CarouselScrollState]
 * @see [rememberCarouselScrollState]
 *
 * @param state state of the scroll
 * @param enabled whether or not scrolling via touch input is enabled
 * @param flingBehavior logic describing fling behavior when drag has finished with velocity. If
 * `null`, default from [ScrollableDefaults.flingBehavior] will be used.
 * @param reverseScrolling reverse the direction of scrolling, when `true`, 0 [CarouselScrollState.value]
 * will mean right, when `false`, 0 [CarouselScrollState.value] will mean left
 */
fun Modifier.horizontalScroll(
    state: CarouselScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
) = scroll(
    state = state,
    isScrollable = enabled,
    reverseScrolling = reverseScrolling,
    flingBehavior = flingBehavior,
    isVertical = false
)

private fun Modifier.scroll(
    state: CarouselScrollState,
    reverseScrolling: Boolean,
    flingBehavior: FlingBehavior?,
    isScrollable: Boolean,
    isVertical: Boolean,
) = composed(
    factory = {
        val coroutineScope = rememberCoroutineScope()
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        // Add RTL to the mix: if horizontal and RTL, reverse reverseScrolling
        val resolvedReverseScrolling =
            if (!isVertical && isRtl) !reverseScrolling else reverseScrolling
        val semantics = Modifier.semantics {
            if (isScrollable) {
                val accessibilityScrollState = ScrollAxisRange(
                    value = { state.value.toFloat() },
                    maxValue = { state.maxValue.toFloat() },
                    reverseScrolling = resolvedReverseScrolling
                )
                if (isVertical) {
                    this.verticalScrollAxisRange = accessibilityScrollState
                } else {
                    this.horizontalScrollAxisRange = accessibilityScrollState
                }
                // when b/156389287 is fixed, this should be proper scrollTo with reverse handling
                scrollBy(
                    action = { x: Float, y: Float ->
                        coroutineScope.launch {
                            if (isVertical) {
                                (state as ScrollableState).scrollBy(y)
                            } else {
                                (state as ScrollableState).scrollBy(x)
                            }
                        }
                        return@scrollBy true
                    }
                )
            }
        }
        val scrolling = Modifier.scrollable(
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
            // reverse scroll to have a "natural" gesture that goes reversed to layout
            reverseDirection = !resolvedReverseScrolling,
            enabled = isScrollable,
            interactionSource = state.internalInteractionSource,
            flingBehavior = flingBehavior,
            state = state
        )
        val layout = ScrollingLayoutModifier(state, reverseScrolling, isVertical)
        semantics
            .then(scrolling)
            .clipScrollableContainer(isVertical)
            .then(layout)
    },
    inspectorInfo = debugInspectorInfo {
        name = "scroll"
        properties["state"] = state
        properties["reverseScrolling"] = reverseScrolling
        properties["flingBehavior"] = flingBehavior
        properties["isScrollable"] = isScrollable
        properties["isVertical"] = isVertical
    }
)

private data class ScrollingLayoutModifier(
    val scrollerState: CarouselScrollState,
    val isReversed: Boolean,
    val isVertical: Boolean,
) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        constraints.assertNotNestingScrollableContainers(isVertical)
        val childConstraints = constraints.copy(
            maxHeight = if (isVertical) Constraints.Infinity else constraints.maxHeight,
            maxWidth = if (isVertical) constraints.maxWidth else Constraints.Infinity
        )
        val placeable = measurable.measure(childConstraints)
        val width = placeable.width.coerceAtMost(constraints.maxWidth)
        val height = placeable.height.coerceAtMost(constraints.maxHeight)
        val scrollHeight = placeable.height - height
        val scrollWidth = placeable.width - width
        val side = if (isVertical) scrollHeight else scrollWidth
        val length = if (isVertical) placeable.height else placeable.width
        return layout(width, height) {
            scrollerState.maxValue = side
            scrollerState.scrollableLength = length
            val scroll = scrollerState.value.coerceIn(0, side)
            val absScroll = if (isReversed) scroll - side else -scroll
            val xOffset = if (isVertical) 0 else absScroll
            val yOffset = if (isVertical) absScroll else 0
            placeable.placeRelativeWithLayer(xOffset, yOffset)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ) = measurable.minIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ) = measurable.minIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ) = measurable.maxIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ) = measurable.maxIntrinsicHeight(width)
}

internal fun Constraints.assertNotNestingScrollableContainers(isVertical: Boolean) {
    if (isVertical) {
        check(maxHeight != Constraints.Infinity) {
            "Nesting scrollable in the same direction layouts like LazyColumn and Column(Modifier" +
                    ".verticalScroll()) is not allowed. If you want to add a header before the list " +
                    "of items please take a look on LazyColumn component which has a DSL api which" +
                    " allows to first add a header via item() function and then the list of " +
                    "items via items()."
        }
    } else {
        check(maxWidth != Constraints.Infinity) {
            "Nesting scrollable in the same direction layouts like LazyRow and Row(Modifier" +
                    ".horizontalScroll() is not allowed. If you want to add a header before the list " +
                    "of items please take a look on LazyRow component which has a DSL api which " +
                    "allows to first add a fixed element via item() function and then the " +
                    "list of items via items()."
        }
    }
}

/**
 * In the scrollable containers we want to clip the main axis sides in order to not display the
 * content which is scrolled out. But once we apply clipToBounds() modifier on such containers it
 * causes unexpected behavior as we also clip the content on the cross axis sides. It is
 * unexpected as Compose components are not clipping by default. The most common case how it
 * could be reproduced is a horizontally scrolling list of Cards. Cards have the elevation by
 * default and such Cards will be drawn with clipped shadows on top and bottom. This was harder
 * to reproduce in the Views system as usually scrolling containers like RecyclerView didn't have
 * an opaque background which means the ripple was drawn on the surface on the first parent with
 * background. In Compose as we don't clip by default we draw shadows right in place.
 * We faced similar issue in Compose already with Androids Popups and Dialogs where we decided to
 * just predefine some constant with a maximum elevation size we are not going to clip. We are
 * going to reuse this technique here. This will improve how it works in most common cases. If the
 * user will need to have a larger unclipped area for some reason they can always add the needed
 * padding inside the scrollable area.
 */
internal fun Modifier.clipScrollableContainer(isVertical: Boolean) =
    then(if (isVertical) VerticalScrollableClipModifier else HorizontalScrollableClipModifier)

private val MaxSupportedElevation = 30.dp

private val HorizontalScrollableClipModifier = Modifier.clip(object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val inflateSize = with(density) { MaxSupportedElevation.roundToPx().toFloat() }
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = -inflateSize,
                right = size.width,
                bottom = size.height + inflateSize
            )
        )
    }
})

private val VerticalScrollableClipModifier = Modifier.clip(object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val inflateSize = with(density) { MaxSupportedElevation.roundToPx().toFloat() }
        return Outline.Rectangle(
            Rect(
                left = -inflateSize,
                top = 0f,
                right = size.width + inflateSize,
                bottom = size.height
            )
        )
    }
})

/**
 * Carousel View.
 *
 * Carousel is a scroll indicator for [ScrollableState] views.
 * This can be added by using [Modifier.verticalScroll], [Modifier.horizontalScroll]
 * which accepts [CarouselScrollState] as a state maintainer.
 *
 * @param state is the state of the scroll using [CarouselScrollState]
 * @param modifier [Modifier] to be applied to the View. If size (width or height) is not
 * set, then it takes the default values [DefaultCarouselWidth] and [DefaultCarouselHeight]
 * for width and height respectively.
 * @param minPercentage is the min percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param maxPercentage is the max percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param colors [CarouselColors] that accepts color and brush for thumb and bg to draw
 * based on the [CarouselScrollState.isScrollInProgress]
 *
 * @see rememberCarouselScrollState
 * @see CarouselScrollState
 * @see Modifier.horizontalScroll
 * @see Modifier.verticalScroll
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 */
@Composable
fun Carousel(
    state: CarouselScrollState,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    minPercentage: Float = DefaultCarouselMinPercentage,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    maxPercentage: Float = DefaultCarouselMaxPercentage,
    colors: CarouselColors = CarouselDefaults.colors(),
) = CarouselImpl(
    scrolled = state.value,
    maxScroll = state.maxValue,
    length = state.scrollableLength,
    modifier = modifier,
    isScrollInProgress = state.isScrollInProgress,
    minPercentage = minPercentage,
    maxPercentage = maxPercentage,
    colors = colors
)

/**
 * Carousel View.
 *
 * Carousel is a scroll indicator for [ScrollableState] views.
 * This can be added by using [LazyRow], [LazyColumn] and [LazyVerticalGrid]
 * which accepts [LazyListState] as a state maintainer.
 *
 * **NOTE: Use this when all items has same length along main axis.**
 *
 * @param state is the state of the scroll using [LazyListState]
 * @param modifier [Modifier] to be applied to the View. If size (width or height) is not
 * set, then it takes the default values [DefaultCarouselWidth] and [DefaultCarouselHeight]
 * for width and height respectively.
 * @param minPercentage is the min percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param maxPercentage is the max percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param colors [CarouselColors] that accepts color and brush for thumb and bg to draw
 * based on the [LazyListState.isScrollInProgress]
 *
 * @see rememberLazyListState
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 */
@Composable
fun Carousel(
    state: LazyListState,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    minPercentage: Float = DefaultCarouselMinPercentage,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    maxPercentage: Float = DefaultCarouselMaxPercentage,
    colors: CarouselColors = CarouselDefaults.colors(),
) {
    val itemLengthInPx = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
    val length = itemLengthInPx * state.layoutInfo.totalItemsCount
    Carousel(
        state = state,
        totalLength = length,
        modifier = modifier,
        minPercentage = minPercentage,
        maxPercentage = maxPercentage,
        colors = colors,
    ) {
        state.firstVisibleItemIndex.times(itemLengthInPx) + state.firstVisibleItemScrollOffset
    }
}

/**
 * Carousel View.
 *
 * Carousel is a scroll indicator for [ScrollableState] views.
 * This can be added by using [LazyRow], [LazyColumn] and [LazyVerticalGrid]
 * which accepts [LazyListState] as a state maintainer.
 *
 * **NOTE: Use this when items are of different sizes and were known to calculate scroll length**
 *
 * @param state is the state of the scroll using [LazyListState]
 * @param totalLength is the total length of all item combined in [Px] along the main axis.
 * @param modifier [Modifier] to be applied to the View. If size (width or height) is not
 * set, then it takes the default values [DefaultCarouselWidth] and [DefaultCarouselHeight]
 * for width and height respectively.
 * @param minPercentage is the min percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param maxPercentage is the max percentage in float in between 0f and 1f that the thumb can
 * be. percentage is with respective to the width of the bar.
 * @param colors [CarouselColors] that accepts color and brush for thumb and bg to draw
 * based on the [LazyListState.isScrollInProgress]
 * @param scrolled is a lambda to calculate the amount that scrolled along main axis in [Px]
 *
 * @see rememberLazyListState
 *
 * @author Sahruday (SAHU)
 *
 * **int a, b = 84, 73**
 */
@Composable
fun Carousel(
    state: LazyListState,
    totalLength: Int,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    minPercentage: Float = DefaultCarouselMinPercentage,
    @FloatRange(from = 0.toDouble(), to = 1.toDouble(), fromInclusive = false, toInclusive = false)
    maxPercentage: Float = DefaultCarouselMaxPercentage,
    colors: CarouselColors = CarouselDefaults.colors(),
    scrolled: () -> Int,
) = CarouselImpl(scrolled = scrolled.invoke(),
    maxScroll = totalLength - state.layoutInfo.viewportEndOffset,
    length = totalLength,
    modifier = modifier,
    isScrollInProgress = state.isScrollInProgress,
    minPercentage = minPercentage,
    maxPercentage = maxPercentage,
    colors = colors)

@Composable
private fun CarouselImpl(
    scrolled: Int,
    maxScroll: Int,
    length: Int,
    modifier: Modifier,
    isScrollInProgress: Boolean,
    minPercentage: Float,
    maxPercentage: Float,
    colors: CarouselColors,
) {
    require(0f < minPercentage) { "min should be > 0f." }
    require(minPercentage <= maxPercentage) { "min should be < max." }
    require(maxPercentage < 1f) { "max should be less than 1f." }
    if (length <= 0 || maxScroll <= 0) return //Will not draw when there is nothing to scroll.

    Canvas(modifier = modifier.size(DefaultCarouselWidth, DefaultCarouselHeight)) {
        val isLtr = layoutDirection == LayoutDirection.Ltr

        val width = drawContext.size.width
        val height = drawContext.size.height

        val isVertical = height > width
        val barLength = if (isVertical) height else width
        val barWidth = if (isVertical) width else height

        val viewportRatio = (length - maxScroll) / length.toFloat() //ViewPort length / length
        val ratio = viewportRatio.coerceIn(minPercentage, maxPercentage)

        val thumbLength = ratio * barLength
        val maxScrollLength = barLength - thumbLength

        val xOffset: Float = (scrolled / maxScroll.toFloat()) * maxScrollLength
        val yOffset = barWidth / 2

        val barStart = if (isLtr) xOffset else maxScrollLength - xOffset
        val barEnd = barStart + thumbLength //if (isLtr) xOffset + thumbWidth else length - xOffset

        fun drawLine(
            brush: Brush,
            startOffSet: Float,
            endOffset: Float,
        ) = drawLine(
            brush = brush,
            start = if (isVertical) Offset(yOffset, startOffSet) else Offset(startOffSet, yOffset),
            end = if (isVertical) Offset(yOffset, endOffset) else Offset(endOffset, yOffset),
            cap = StrokeCap.Round,
            strokeWidth = barWidth,
        )

        //Draw Background
        drawLine(colors.backgroundBrush(isScrollInProgress), 0f, barLength)

        //Draw Thumb
        drawLine(colors.thumbBrush(isScrollInProgress), barStart, barEnd)
    }
}

/**
 * Default maximum percentage of the thumb to occupy in the bar.
 */
const val DefaultCarouselMaxPercentage = 0.8f

/**
 * Default minimum percentage of the thumb to occupy in the bar.
 */
const val DefaultCarouselMinPercentage = 0.2f

/**
 * Default width when no width constraint is added using modifier
 */
val DefaultCarouselWidth = 60.dp

/**
 * Default height when no height constraint is added using modifier
 */
val DefaultCarouselHeight = 4.dp

/**
 * Represents the colors and brushes used by the [Carousel].
 *
 * @see CarouselDefaults.colors
 */
interface CarouselColors {

    /**
     * Represents the brush used to draw the carousel thumb line, depending on [isScrollInProgress].
     *
     * Thumb line uses brush only when when it is not null.
     *
     * @param isScrollInProgress is weather the scroll is action or not
     *
     * @see [ScrollableState.isScrollInProgress]
     */
    fun thumbBrush(isScrollInProgress: Boolean): Brush

    /**
     * Represents the brush used to draw the carousel bg line, depending on [isScrollInProgress].
     *
     * Background line uses brush only when when it is not null.
     *
     * @param isScrollInProgress is weather the scroll is action or not
     *
     * @see [ScrollableState.isScrollInProgress]
     */
    fun backgroundBrush(isScrollInProgress: Boolean): Brush

}

object CarouselDefaults {

    @Composable
    fun colors(
        thumbBrush: Brush,
        scrollingThumbBrush: Brush = thumbBrush,
        backgroundBrush: Brush,
        scrollingBackgroundBrush: Brush = backgroundBrush,
    ): CarouselColors = DefaultCarousalColors(
        thumbBrush = thumbBrush,
        scrollingThumbBrush = scrollingThumbBrush,
        backgroundBrush = backgroundBrush,
        scrollingBackgroundBrush = scrollingBackgroundBrush
    )

    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colors.secondary,
        scrollingThumbColor: Color = thumbColor,
        backgroundColor: Color = contentColorFor(thumbColor).copy(alpha = BgAlpha),
        scrollingBackgroundColor: Color = backgroundColor,
    ): CarouselColors = DefaultCarousalColors(
        thumbBrush = SolidColor(thumbColor),
        scrollingThumbBrush = SolidColor(scrollingThumbColor),
        backgroundBrush = SolidColor(backgroundColor),
        scrollingBackgroundBrush = SolidColor(scrollingBackgroundColor)
    )

    const val BgAlpha = 0.25f
}

@Immutable
private class DefaultCarousalColors(
    private val thumbBrush: Brush,
    private val scrollingThumbBrush: Brush,
    private val backgroundBrush: Brush,
    private val scrollingBackgroundBrush: Brush,
) : CarouselColors {

    override fun thumbBrush(isScrollInProgress: Boolean): Brush =
        if (isScrollInProgress) thumbBrush else scrollingThumbBrush

    override fun backgroundBrush(isScrollInProgress: Boolean): Brush =
        if (isScrollInProgress) backgroundBrush else scrollingBackgroundBrush

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultCarousalColors

        if (thumbBrush != other.thumbBrush) return false
        if (scrollingThumbBrush != other.scrollingThumbBrush) return false
        if (backgroundBrush != other.backgroundBrush) return false
        if (scrollingBackgroundBrush != other.scrollingBackgroundBrush) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbBrush.hashCode()
        result = 31 * result + scrollingThumbBrush.hashCode()
        result = 31 * result + backgroundBrush.hashCode()
        result = 31 * result + scrollingBackgroundBrush.hashCode()
        return result
    }
}