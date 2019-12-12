# Project


The main idea of this project is for doctor to monitor patient without actually being present near him

Main components of this app includes:
1. BLE sensor for heart rate
2. Doctor able to fetch the heart rate data and monitor patient
3. Aws amplify(notification and auth)


Currently we are using polar BLE sensor to get heart rate from the user which updates to firebase frequently

After every hour the values are aggregated and updated to firebase there.
After that data is moved to database for storage for faster access and it won't put much load on the app.
Using thread we collect data every minute and aggregate data.
Users can see current session data and previous sessions data as well.

Main features:

1. Doctor can continously monitor heart rate of all patients that have subscribed.
2. patient can add emergency contact to the list to send notifcations in case of emergency
3. Through SNS we send notification to mobile endpoint as well.
4. Notification opens the app directly to details page
5. you can scan ble devices around you

BLE we used to get Heart Rate:
1. Polar BLE 
2. Polar sensor : https://www.amazon.com/Polar-Heart-Rate-Monitor-Women/dp/B07PM565W2/ref=asc_df_B07PM565W2/?tag=hyprod-20&linkCode=df0&hvadid=343194339339&hvpos=1o1&hvnetw=g&hvrand=11893705533486593787&hvpone=&hvptwo=&hvqmt=&hvdev=c&hvdvcmdl=&hvlocint=&hvlocphy=9009981&hvtargid=pla-710982969984&psc=1&tag=&ref=&adgrpid=69473715059&hvpone=&hvptwo=&hvadid=343194339339&hvpos=1o1&hvnetw=g&hvrand=11893705533486593787&hvqmt=&hvdev=c&hvdvcmdl=&hvlocint=&hvlocphy=9009981&hvtargid=pla-710982969984

3 . Polar has it's own sdk for andriod to scan data.
4. Polar sdk link : https://www.polar.com/en/developers/sdk

AWS Services:
1. AWS Cognito (to get authentication for aws services)
2. AWS SES (for email confirmation)
3. AWS SNS(For notification service)
4. you can setup in andriod environment using Amplify(git link : https://github.com/aws-amplify/aws-sdk-android)
5. you need to create amplify project in the root directory of the environment
