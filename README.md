MobilityPhone
=============

MobilityPhone uses the google play services [ActivityRecognitionClient] to classify a activity. It
sends points to the ohmage 3.0 client on the phone to be uploaded to a server.

The app captures two ohmage streams: [Activity] and [Location]. Each stream is uploaded independently and each stream may be controlled independently.

For Location, the maximum frequency of data collection is every minute. For Activity, the maximum frequency of data collection is dependent on the Google Play Services activity classifier.

[ActivityRecognitionClient]: http://developer.android.com/reference/com/google/android/gms/location/ActivityRecognitionClient.html
[Activity]: https://dev.ohmage.org/stream/ba902741-3f4b-4909-a15a-f799ba36469b
[Location]: https://dev.ohmage.org/stream/8131a709-9342-47f8-b893-dcf9c824342c
