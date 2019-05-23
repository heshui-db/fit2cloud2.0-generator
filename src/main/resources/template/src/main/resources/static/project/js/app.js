/**
 * 启动app，加载菜单
 */

// 流程管理使用方法：1、加载process-design.css和process-design.js，加载f2c.process，配置menu.json、permission.json
var ProjectApp = angular.module('ProjectApp', ['f2c.common']);


ProjectApp.controller('IndexCtrl', function ($scope) {
    $scope.menus = [];
});
