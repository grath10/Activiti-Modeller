/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';
var activitiModeler = angular.module('activitiModeler', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngDragDrop',
  'mgcrea.ngStrap',
  'mgcrea.ngStrap.helpers.dimensions', // Needed for tooltips
  'ui.grid',
  'ui.grid.edit',
  'ui.grid.selection',
  'ui.grid.autoResize',
  'ui.grid.moveColumns',
  'ui.grid.cellNav',
  'ngAnimate',
  'pascalprecht.translate',
  'ngFileUpload',
  'angularSpectrumColorpicker',
  'duScroll',
  'dndLists'
]);

var activitiModule = activitiModeler;
var activitiApp = activitiModeler;

activitiModeler
  // Initialize routes
  .config(['$provide', '$routeProvider', '$selectProvider', '$translateProvider', function ($provide, $routeProvider, $selectProvider, $translateProvider) {

    var appName = 'editor';
    $provide.value('appName', appName);
    var appResourceRoot = '/' + appName + '/';
    $provide.value('appResourceRoot', appResourceRoot);

      // Override caret for bs-select directive
      angular.extend($selectProvider.defaults, {
          caretHtml: '&nbsp;<i class="icon icon-caret-down"></i>'
      });

      var authRouteResolver = ['$rootScope', function($rootScope) {
          // Authentication done on rootscope, no need to call service again. Any unauthenticated access to REST will result in
          // a 401 and will redirect to login anyway. Done to prevent additional call to authenticate every route-change
          $rootScope.authenticated = true;
          return true;
      }];

        $routeProvider
            .when('/processes', {
                templateUrl: appResourceRoot + 'views/processes',
                controller: 'ProcessesCtrl',
                resolve: {
                    verify: authRouteResolver
                }
            })
            .when('/processes/:modelId', {
                templateUrl: appResourceRoot + 'views/process',
                controller: 'ProcessCtrl'
            })
            .when('/processes/:modelId/history/:modelHistoryId', {
                templateUrl: appResourceRoot + 'views/process',
                controller: 'ProcessCtrl'
            })
            .when('/forms', {
                templateUrl: appResourceRoot + 'views/forms',
                controller: 'FormsCtrl'
            })
            .when('/forms/:modelId', {
                templateUrl: appResourceRoot + 'views/form',
                controller: 'FormCtrl'
            })
            .when('/forms/:modelId/history/:modelHistoryId', {
                templateUrl: appResourceRoot + 'views/form',
                controller: 'FormCtrl'
            })
            .when('/editor/:modelId', {
                templateUrl: appResourceRoot + 'views/editor',
                controller: 'EditorController'
            })
	        .when('/form-editor/:modelId', {
	            templateUrl: appResourceRoot + 'views/form-builder',
	            controller: 'FormBuilderController'
	        });
            
        if (ACTIVITI.CONFIG.appDefaultRoute) {
            $routeProvider.when('/', {
                redirectTo: ACTIVITI.CONFIG.appDefaultRoute
            });
        } else {
            $routeProvider.when('/', {
                redirectTo: '/processes',
                resolve: {
                    verify: authRouteResolver
                }
            })
        }

        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
          prefix: '/editor/i18n/',
          suffix: '.json'
        });
        $translateProvider.preferredLanguage('zh-CN');
        $translateProvider.registerAvailableLanguageKeys(['en', 'zh'], {
            'en_*': 'en',
            'zh-*': 'zh-CN'
        });
        
  }])
  .run(['$rootScope', '$timeout', '$modal', '$translate', '$location', '$window', 'appResourceRoot',
        function($rootScope, $timeout, $modal, $translate, $location, $window, appResourceRoot) {

            $rootScope.restRootUrl = function() {
                return ACTIVITI.CONFIG.contextRoot;
            };

          $rootScope.appResourceRoot = appResourceRoot;

            $rootScope.window = {};
            var updateWindowSize = function() {
                $rootScope.window.width = $window.innerWidth;
                $rootScope.window.height  = $window.innerHeight;
            };

            // Window resize hook
            angular.element($window).bind('resize', function() {
                $rootScope.safeApply(updateWindowSize());
            });

            $rootScope.$watch('window.forceRefresh', function(newValue) {
                if(newValue) {
                    $timeout(function() {
                        updateWindowSize();
                        $rootScope.window.forceRefresh = false;
                    });
                }
            });

            updateWindowSize();

            // Main navigation
            $rootScope.mainNavigation = [
                {
                    'id': 'processes',
                    'title': 'GENERAL.NAVIGATION.PROCESSES',
                    'path': '/processes'
                },
                {
                    'id': 'forms',
                    'title': 'GENERAL.NAVIGATION.FORMS',
                    'path': '/forms'
                }
            ];

            $rootScope.config = ACTIVITI.CONFIG;

            $rootScope.mainPage = $rootScope.mainNavigation[0];

            /*
             * History of process and form pages accessed by the editor.
             * This is needed because you can navigate to sub processes and forms
             */
            $rootScope.editorHistory = [];

            /*
             * Set the current main page, using the page object. If the page is already active,
             * this is a no-op.
             */
            $rootScope.setMainPage = function(mainPage) {
                $rootScope.mainPage = mainPage;
                $location.path($rootScope.mainPage.path);
            };

            /*
             * Set the current main page, using the page ID. If the page is already active,
             * this is a no-op.
             */
            $rootScope.setMainPageById = function(mainPageId) {
                for (var i=0; i<$rootScope.mainNavigation.length; i++) {
                    if (mainPageId == $rootScope.mainNavigation[i].id) {
                        $rootScope.mainPage = $rootScope.mainNavigation[i];
                        break;
                    }
                }
            };

            /**
             * A 'safer' apply that avoids concurrent updates (which $apply allows).
             */
            $rootScope.safeApply = function(fn) {
                var phase = this.$root.$$phase;
                if(phase == '$apply' || phase == '$digest') {
                    if(fn && (typeof(fn) === 'function')) {
                        fn();
                    }
                } else {
                    this.$apply(fn);
                }
            };

            // Alerts
            $rootScope.alerts = {
                queue: []
            };

            $rootScope.showAlert = function(alert) {
                if(alert.queue.length > 0) {
                    alert.current = alert.queue.shift();
                    // Start timeout for message-pruning
                    alert.timeout = $timeout(function() {
                        if (alert.queue.length == 0) {
                            alert.current = undefined;
                            alert.timeout = undefined;
                        } else {
                            $rootScope.showAlert(alert);
                        }
                    }, (alert.current.type == 'error' ? 5000 : 1000));
                } else {
                    $rootScope.alerts.current = undefined;
                }
            };

            $rootScope.addAlert = function(message, type) {
                var newAlert = {message: message, type: type};
                if (!$rootScope.alerts.timeout) {
                    // Timeout for message queue is not running, start one
                    $rootScope.alerts.queue.push(newAlert);
                    $rootScope.showAlert($rootScope.alerts);
                } else {
                    $rootScope.alerts.queue.push(newAlert);
                }
            };

            $rootScope.dismissAlert = function() {
                if (!$rootScope.alerts.timeout) {
                    $rootScope.alerts.current = undefined;
                } else {
                    $timeout.cancel($rootScope.alerts.timeout);
                    $rootScope.alerts.timeout = undefined;
                    $rootScope.showAlert($rootScope.alerts);
                }
            };

            $rootScope.addAlertPromise = function(promise, type) {
                if (promise) {
                    promise.then(function(data) {
                        $rootScope.addAlert(data, type);
                    });
                }
            };

            // Edit profile and change password
            $rootScope.editProfile = function() {
                _internalCreateModal({
                    template: 'views/popup/account-edit.html'
                }, $modal, $rootScope);
            };

            $rootScope.changePassword = function() {
                _internalCreateModal({
                    template: 'views/popup/account-change-password.html'
                }, $modal, $rootScope);
            };
        }
  ])
    // Moment-JS date-formatting filter
    .filter('dateformat', function() {
        return function(date, format) {
            var locale = window.navigator.userLanguage || window.navigator.language;
            moment.lang(locale.toLowerCase());
            if (date) {
                if (format) {
                    return moment(date).format(format);
                } else {
                    return moment(date).calendar();
                }
            }
            return '';
        };
    });