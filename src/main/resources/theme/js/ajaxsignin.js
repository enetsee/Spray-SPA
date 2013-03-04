$(function () {

    $('#signupForm').validate({
    	    rules: {
                  name: {minlength: 2,required: true},
                  email: {required: true,email: true},
                  password: {minlength: 6, required: true}
    	    },
           	highlight: function(element) {
           				$(element).closest('.control-group').removeClass('success').addClass('error');
           			},
           			success: function(element) {
           				element
           				.addClass('valid')
           				.closest('.control-group').removeClass('error').addClass('success');
           				element.remove();
           			}
          });



    var displayErrors = function(form,errors) {
        var errorSummary = form.find('.validation-summary');
        var items = $.map(errors, function(err) {
            return '<li><div class=\"alert alert-error\">' + err + '</div></li>';
       }).join('');

        errorSummary.find('ul').empty().append(items);
    };


    var formSubmitHandler = function (e) {
        var $form = $(this);
        // We check if jQuery.validator exists on the form
        if (!$form.valid || $form.valid()) {
            $.post($form.attr('action'), $form.serialize())
                .done(function (json) {
                    json = json || {};
                    // In case of success, we redirect to the provided URL or the same page.
                    if (json.success) {
                        window.location = json.redirect || location.href;
                    } else if (json.errors) {
                        displayErrors($form, json.errors);
                    }
                })
                .error(function () {
                    displayErrors($form, ['An unknown error occurred.']);
                });
        }
        // Prevent the normal behavior since we opened the dialog
        e.preventDefault();
    };


    $("#signinForm").submit(formSubmitHandler);
    $("#signupForm").submit(formSubmitHandler);
});