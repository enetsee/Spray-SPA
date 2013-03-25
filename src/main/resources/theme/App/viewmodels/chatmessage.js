define(['services/logger'], function (logger) {

    var ChatMessage= function(data) {
        this.message = data.message;
    };

    return (ChatMessage);
});