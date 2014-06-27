MobilityPhone
=============

MobilityPhone uses the Google Play Services' [ActivityRecognitionClient] to periodically classify a user's activity. It
sends classified data points to the ohmage 3.0 client on the phone which then uploads them to an ohmage server.

The app captures two ohmage streams: [Activity] and [Location]. Each stream is uploaded independently and each stream may be controlled independently.

For Location, the maximum frequency of data collection is every minute. For Activity, the maximum frequency of data collection is dependent on the Google activity classifier (2-3 times per minute is a good bound on the max).

[ActivityRecognitionClient]: http://developer.android.com/reference/com/google/android/gms/location/ActivityRecognitionClient.html
[Activity]: https://dev.ohmage.org/stream/ba902741-3f4b-4909-a15a-f799ba36469b
[Location]: https://dev.ohmage.org/stream/8131a709-9342-47f8-b893-dcf9c824342c
