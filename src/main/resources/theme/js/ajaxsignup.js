$(function () {
    ko.validation.configure({
        insertMessages: true,
        decorateElement: true,
        errorElementClass: 'error'
    });

    function SignUpViewModel() {
        var self = this;
        self.name = ko.observable('').extend({minLength: {params:2,message:'please enter at least 2 characters'}});
        self.email = ko.observable('').extend({ email: {params:true,message:'please enter a valid email address'}});
        self.password = ko.observable('').extend({minLength : {params:6,message:'please enter at least 6 characters'}});
        self.working = ko.observable(false);
        self.alerts  = ko.observableArray([]);
        self.errors = ko.validation.group(self);

        self.updateAlerts = function(errors) {
            self.alerts([]);
            $.map(errors , function(err) { self.alerts.push(err);} );
        }

        // Response handlers
        // 201 response handler
        self.Created = function(data,textStatus,jqXHR) { window.location = '/'; };

        // 400 response handler
        self.BadRequest = function(jqXHR,textStatus,errorThrown) {
            self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };
        // 500 response handler
        self.InternalServerError = function(jqXHR, textStatus,errorThrown) {
             self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };
        //503 response handler
        self.ServiceUnavailable = function(jqXHR, textStatus,errorThrown) {
            self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };


        self.signUp = function() {
            self.working(true);
            $.ajax("/ajax/signup",
                {data: ko.toJSON({name:self.name,email:self.email,password:self.password})
                , type: "post"
                , contentType: "application/json"
                , statusCode: {
                    201: self.Created,
                    400: self.BadRequest,
                    500: self.InternalServerError,
                    503: self.ServiceUnavailable
                  }
                }
            ).always(function() { self.working(false);  });
        };
    };

    ko.applyBindings(new SignUpViewModel());
});