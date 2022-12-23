package dev.zwander.cellreader.data.data

class CellModelWear private constructor() : CellModelBase() {
    companion object {
        private var instance: CellModelWear? = null

        fun getInstance(): CellModelWear {
            return instance ?: CellModelWear().apply { instance = this }
        }
    }
}