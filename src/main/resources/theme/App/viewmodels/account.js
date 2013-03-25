define(['services/logger'], function (logger) {
    var Account= function(data) {
              this.id = data.id;
              this.name = ko.observable(data.name);
              this.email = ko.observable(data.email);
          };
    return Account
});