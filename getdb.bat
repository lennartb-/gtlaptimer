adb -d shell "run-as org.lennartb.gtlaptimer cat /data/data/org.lennartb.gtlaptimer/databases/gtlaptimer.db > /sdcard/database.sqlite"
adb pull /sdcard/database.sqlite ./gtlaptimer.db