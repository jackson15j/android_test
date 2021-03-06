My Android Test app
===================

Just a random app that I'm using to learn both Java and Android 
development. Main aim is to be able to do a GET/POST to a web service
with Oauth when required. The GET's should be displayed to the user via
the UI.

![Sample screenshot](extras/screenshot.png "Sample screenshot")

Link Dump
=========

In no particular order, here is a link dump provided by my 
investigations and from talks with a friendly android contractor:

* http://search.maven.org - Java equivalent of Python's PyPi.

Withings
--------

* http://oauth.withings.com/api
* http://oauth.withings.com/api/doc#api-Measure-get_activity

Fitbit
------

* https://dev.fitbit.com/docs/basics/

Github
------

* https://developer.github.com/v3/

Android Studio
--------------

* Get *markdown navigator* for native editing of markdown files.
* Get *Genymotion* if you don't want to use built in emulators.
* If you don't see a preview on layout files, check the version drop
  down above the preview pane. It's probably on an incompatible version
  (Like android N).
* Apparently real Android dev's still use Android Studio.
* https://developer.android.com/training/index.html
* http://tools.android.com/tech-docs/new-build-system/user-guide
* https://docs.gradle.org/current/userguide/build_environment.html
* http://pkaq.github.io/gradledoc/docs/userguide/tutorial_this_and_that.html

Oauth
-----

* http://oauth.net/code/
* https://futurestud.io/blog/oauth-2-on-android-with-retrofit - Highly recommend!!
* https://cloud.google.com/appengine/docs/java/oauth/
* https://github.com/bkiers/retrofit-oauth - demo

REST/HTTP Libraries
-------------------

Apparently [Square](https://square.github.io/) makes the best libraries
for accessing API's.

* https://square.github.io/okhttp/ 
  [[Github](https://github.com/square/okhttp)]- Version 3 has lots of 
  changes but is now mainstream.
* http://square.github.io/retrofit/
  [[Github](https://github.com/square/retrofit)] - Think Python's
  `requests` library. Version 2 has lots of changes but is now
  mainstream, but poorly documented.
* http://square.github.io/retrofit/2.x/retrofit/
* https://futurestud.io/blog/retrofit-getting-started-and-android-client - Highly recommend!!
* https://realm.io/news/droidcon-jake-wharton-simple-http-retrofit-2/
* https://github.com/codepath/android_guides/wiki/Consuming-APIs-with-Retrofit
* https://github.com/codepath/android_guides/wiki/Using-OkHttp
* http://themakeinfo.com/2015/04/retrofit-android-tutorial/

JSON
----

* http://www.jsonschema2pojo.org

Threading
---------

* https://github.com/codepath/android_guides/wiki/Creating-and-Executing-Async-Tasks
* https://developer.android.com/reference/android/os/AsyncTask.html
* https://github.com/codepath/android_guides/wiki/Managing-Threads-and-Custom-Services#handler-and-loopers
* http://simonvt.net/2014/04/17/asynctask-is-bad-and-you-should-feel-bad/
  - interesting article which moves you from basics of AsyncTask, onto
  using a bus to separate business logic from UI and allow message
  queueing.
* http://square.github.io/otto/ - Event Bus that can be used with
  AsyncTask.

Images
------

* http://square.github.io/picasso/ - Display an image from a url string
  in one-line. Nice!

Android
-------

There are some gotcha's with android development that are not always
explicitly stated in generic Java examples, such as:

* The main thread is UI only. Most libraries (eg. Retrofit) will
  raise a network exception if you try to do network calls in the UI
  thread. To fix, you must create a new thread. Async-Tasks library is 
  one way of doing this easily.
* Common behaviour for files to read config from (eg.
  `local.properties`), is to place them in either:
  `app/src/main/assets`, or: `app/src/main/res/raw`. Others have said
  root of the project, but haven't gotten this to work for me yet.
* The [intent] mechanism is Android speak for different processes and
  how data is passed between those processes.
* [intent-filters] are used to register your application to handle
  specific scenarios. One example of where this is needed is in Oauth.
  We need the redirect uri to come back to the application. This
  requires the redirect uri to have a schema which the Browser/WebView
  doesn't understand, so that it looks up an application which can
  handle it. Else we will be trapped in the Browser/WebView.
* If you have already made an Oauth request and it has been set to:
  `allow`, then I think there needs to be some code to handle that
  gracefully. Currently hanging the Browser if it has been left open
  between subsequent Oauth request attempts.
* Might have missed it, but it looks like there is no nice method in
  `Uri` to parse key/value pairs out of a uri with a fragment
  (schema://host#key=value&...). If you have a uri with a query
  (schema://host?key=value&...), then you can use:
  `String value = Uri.getQueryParameter("key");`.

Books
=====

* Effective Java (2nd Edition) by Joshua Bloch.

[intent]: https://developer.android.com/reference/android/content/Intent.html
[intent-filters]: https://developer.android.com/guide/components/intents-filters.html