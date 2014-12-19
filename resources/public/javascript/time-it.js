(function () {
  'use strict';
  var app = angular.module('timeIt', ['ngRoute'], function($httpProvider) {
    $httpProvider.interceptors.push('AuthInterceptor');
  });

  app.constant('API_URL', 'http://localhost:3060/api');
  app.constant('UNRESTRICTED_PATHS', ['/login', '/signup']);

  app.run(function($rootScope, AuthTokenFactory, $location, UNRESTRICTED_PATHS) {
    $rootScope.$on('$routeChangeStart', function(event, next) {
      var token = AuthTokenFactory.getToken();
      if (!token && UNRESTRICTED_PATHS.indexOf($location.$$path) < 0) {
        $location.path('/login');
      }
    });
  });

  app.config(function($routeProvider) {
    $routeProvider.when('/', {
      redirectTo: '/home'
    })
    .when('/home', {
      controller: 'HomeCtrl',
      templateUrl: 'home.html'
    })
    .when('/login', {
      controller: 'LoginCtrl',
      templateUrl: 'login.html'
    })
    .when('/signup', {
      controller: 'SignupCtrl',
      templateUrl: 'signup.html'
    });
  });

  app.controller('HomeCtrl', function($scope) {

  });

  app.controller('LoginCtrl', function($scope, UserFactory, $location) {
    $scope.credentials = {
      email: '',
      password: ''
    };

    $scope.login = function(credentials) {
      UserFactory.login(credentials.email, credentials.password).then(function success() {
        $location.path('/home');
      }, handlerError);
    };

    function handlerError(response) {
      alert('Error: ' + response.data);
    }
  });

  app.controller('SignupCtrl', function($scope) {

  });

  app.factory('UserFactory', function($http, API_URL, AuthTokenFactory, $q) {
    'use strict';
    return {
      login: login,
      logout: logout,
      getUser: getUser
    };

    function login(email, password) {
      return $http({
        url: API_URL + '/tokens',
        method: 'GET',
        params: {
          email: email,
          password: password
        }
      }).then(function success(response) {
        AuthTokenFactory.setToken(response.data.token);
        return response;
      });
    }

    function logout() {
      AuthTokenFactory.setToken();
    }

    function getUser() {
      if (AuthTokenFactory.getToken()) {
        return $http.get(API_URL + '/users');
      } else {
        return $q.reject({
          data: 'client has no auth token'
        });
      }
    }
  });

  app.factory('AuthTokenFactory', function($window) {
    'use strict';
    var store = $window.localStorage;
    var key = 'auth-token';

    return {
      getToken: getToken,
      setToken: setToken
    };

    function getToken() {
      return store.getItem(key);
    }

    function setToken(token) {
      if (token) {
        store.setItem(key, token);
      } else {
        store.removeItem(key);
      }
    }
  });

  app.factory('AuthInterceptor', function(AuthTokenFactory) {
    'use strict';
    return {
      request: addToken
    };

    function addToken(config) {
      var token = AuthTokenFactory.getToken();
      if (token) {
        config.headers = config.headers || {};
        config.headers.Authorization = 'Bearer ' + token;
      }
      return config;
    }
  });
})();
