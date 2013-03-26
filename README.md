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

I would really appreciate feedback/suggestions on the design - I check out the [spray-user](https://groups.google.com/forum/?fromgroups=#!forum/spray-user) google group fairly regularly; makes sense to discuss this there.
