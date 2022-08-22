# CellReader
CellReader is an app to let you view information about your current cellular connection and other available connections.

With CellReader you can:
- See which band you're currently connected to.
- See the technology of the cellular network you're connected to.
- See all nearby towers reported by the modem.
- View information about your cellular registration status.
- And more.

There is also a Wear OS companion app, although its functionality is in early alpha.

# Privacy
CellReader doesn't collect or store any data beyond what's needed to record crashes. No personal information is collected.

The following permissions are needed for functionality:
- Precise Location
  - Many modem APIs require this permission. Your location isn't recorded or uploaded anywhere.
- Phone State
  - This is another permission required by modem APIs. No information about your device is recorded or uploaded.
- Phone Numbers
  - This permission is a subset of the "Phone State" permission and automatically granted when that one is granted. This allows CellReader to view your phone number to display in the app.
  - Your phone number isn't recorded or uploaded.