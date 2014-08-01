(function() {

    angular.module('mooseheadModule', []);

    var bootstrap;
    bootstrap = function() {
        angular.module('moosehead', ['mooseheadModule']).
        config(['$routeProvider', function($routeProvider) {
                $routeProvider
                    .when('/', {
                        templateUrl: 'templates/workshopList.html',
                        controller: 'WorkshopListCtrl'
                    })
                    .when("/register/:workshopid", {
                        templateUrl: 'templates/register.html',
                        controller: 'RegisterCtrl'
                    }).when("/cancel/:token", {
                        templateUrl: 'templates/cancel.html',
                        controller: 'CancelCtrl'
                    }).when("/showMyReservations/:email", {
                        templateUrl: 'templates/showMyReservations.html',
                        controller: 'ShowMyReservationCtrl'
                    })
                    ;
        }]);
        
        angular.bootstrap(document,['moosehead']);
        
    };

    bootstrap();


}());
