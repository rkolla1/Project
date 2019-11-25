# Project


The main idea of this project is for doctor to monitor patient without actually being present near him

Main components of this app includes:
1. BLE sensor for heart rate
2. Doctor able to fetch the heart rate data and monitor patient


Currently we are using polar BLE sensor to get heart rate from the user which updates to firebase frequently

After every hour the values are aggregated and updated to firebase and stored for 1 day.
After that data is moved to database for storage for faster access and it won't put much load on the app.

Main features:

1. Doctor can continously monitor heart rate of all patients that have subscribed.
2. patient can add emergency contact to the list to send notifcations in case of emergency
3. Doctor can message user everyday using inbuilt app messaging system

