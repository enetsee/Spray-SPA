Spray-SPA (Spray Single-Page Application)
=========================================

This project is intended to demonstrate how to approach building a Spray based application which provides both a RESTful programmatic api and a 'user-focused' (ajax) api on which a client-side app can be built.

I get the sense that there is a demand for this kind of thing; the [Play book](http://www.manning.com/hilton/) even has a chapter dedicated to single-page apps, but I would have thought this approach is a good fit for Spray too.

The project includes some boilerplate around account management; in particular:
- 	a generic account type (inspired by [Snap](https://github.com/snapframework/snap/blob/master/src/Snap/Snaplet/Auth/Types.hs))
-	sign-in, sign-up an sign-out
-	password encryption (using [JBCrypt](https://code.google.com/p/jbcrypt/), an open-source implementation of [BCrypt](http://en.wikipedia.org/wiki/Bcrypt))
- 	session cookies (based on [Play](https://github.com/playframework/Play20) session cookies)
-	persistent login ('remember-me') cookies (with an approach based on [Improved Persistent Login Cookie Best Practice](http://jaspan.com/improved_persistent_login_cookie_best_practice))

The project uses [Slick](http://slick.typesafe.com/) for persistence, an approach demonstrated in the excellent [S4](https://github.com/jacobus/s4) starter project, and [Twirl](https://github.com/spray/twirl) for templating.

The client side app is built with [Durandal.js](http://durandaljs.com/) which itself is built on [Knockout.js](knockoutjs.com), [Require.js](http://requirejs.org/) and the ubiquitous [jQuery](http://jquery.com/). I know next to nothing about javascript MVC frameworks so I took the approach of just picking one and working with it (and Durandal was suprisingly painless).


Further work
------------

More account management features:
-	account activation using [Spray Client](https://github.com/spray/spray/wiki/spray-client) and a third-party API (e.g. [SendGrid](http://sendgrid.com/))
-	account reset
-	account suspension

It would be good to include an actual application; the intention is to provide a best-practice REST api in addition to a ajax oriented API. My understanding of how this should be arranged is fuzzy at best. I have been inspecting how the [stripe](http://stripe.com) dashboard is implemented and the api calls it is making (vs. the published REST api) and using that as a guide.

If anyone would like to suggest a fun application to run with let me know, otherwise I'll move forward with a To-do app or somesuch.

I would also like to make this an end-to-end project and show:
-	testing 
-	non-trivial akka and spray configuration 
-	logging
-	properly structured spray app (i.e. multi-project, clearly delineating application and api)
-	deployment to EC2

I don't know how hard it would be to do but a Play-style asset pipeline would be quite cool too.

I am probably going to need some help with all of that...


Contribute
----------

This is something of a learning exercise for me; I expect there is significant room for improvement both in terms of how the application is structured and how I am approaching the account management functionality.

I'm putting this out there as a starting point - please feel free to tear the design apart and tell me what I'm doing wrong. 

If any one can suggest a better way or think of useful features, I would very much like to hear from you - pull requests are most welcome.


Discussion 
----------

I check out the [spray-user](https://groups.google.com/forum/?fromgroups=#!forum/spray-user) google group fairly regularly; makes sense to discuss this there.
