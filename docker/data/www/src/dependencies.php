<?php
use Slim\App;

return function (App $app) {
    $container = $app->getContainer();
    // view renderer
    $container['renderer'] = function ($c) {
        $settings = $c->get('settings')['renderer'];
        return new \Slim\Views\PhpRenderer($settings['template_path']);
    };
};
