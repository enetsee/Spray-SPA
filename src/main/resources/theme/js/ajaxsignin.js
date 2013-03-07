$(function () {
    ko.validation.configure({
        insertMessages: false,
        decorateElement: true,
        errorElementClass: 'error'
    });

    function SignInViewModel() {
        var self = this;
        self.email = ko.observable('').extend({ email: true}).extend({required:true});
        self.password = ko.observable('').extend({required:true});
        self.rememberMe = ko.observable(false);
        self.working = ko.observable(false);
        self.alerts  = ko.observableArray([]);
        self.errors = ko.validation.group(self);

        // Response handlers
        // 200 response handler
        self.OK = function(data,textStatus,jqXHR) { window.location = '/'; };

        // 400 response handler
        self.BadRequest = function(jqXHR,textStatus,errorThrown) {
            self.alerts([]);
            $.map(  $.parseJSON(jqXHR.responseText).errors , function(err) { self.alerts.push(err);} );
        };
        // 401 response handler
        self.Unauthorized = function(jqXHR,textStatus,errorThrown) {
            self.alerts([]);
            $.map(  $.parseJSON(jqXHR.responseText).errors , function(err) { self.alerts.push(err);} );
        };
        // 500 response handler
        self.InternalServerError = function(jqXHR, textStatus,errorThrown) {
             self.alerts([]);
             $.map(  $.parseJSON(jqXHR.responseText).errors , function(err) { self.alerts.push(err);} );
        };
        //503 response handler
        self.ServiceUnavailable = function(jqXHR, textStatus,errorThrown) {
            self.alerts([]);
            $.map(  $.parseJSON(jqXHR.responseText).errors , function(err) { self.alerts.push(err);} );
        };

        self.signin = function() {
            self.working(true);
            $.ajax("/ajax/signin",
                {data: ko.toJSON({email:self.email,password:self.password,rememberMe:self.rememberMe})
                , type: "post"
                , contentType: "application/json"
                , statusCode: {
                    200: self.OK,
                    400: self.BadRequest,
                    401: self.Unauthorized,
                    500: self.InternalServerError,
                    503: self.ServiceUnavailable
                  }
                }
            ).always(function() { self.working(false);  });
        };
    };

    ko.applyBindings(new SignInViewModel());
});