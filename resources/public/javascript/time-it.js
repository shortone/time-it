angular.module('timeIt', ['ngRoute'])
.config(function($routeProvider) {
  $routeProvider
  .when('/', {
    redirectTo: '/login'
  })
  .when('/login', {
    controller: 'LoginCtrl',
    templateUrl: 'login.html'
  });
})
.controller('HomeCtrl', function($scope) {

})
.controller('LoginCtrl', function($scope) {

});
