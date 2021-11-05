<?php
use Slim\App;
use Slim\Http\Request;
use Slim\Http\Response;
use controllers\HomeController;

return function (App $app) {
    $app->get('/', HomeController::class . ':home');
    $app->get('/devices', HomeController::class . ':get_devices');
    $app->post('/devices', HomeController::class . ':set_devices');
    $app->post('/start_cam_feed', HomeController::class . ':start_cam_feed');
    $app->post('/start_mic_feed', HomeController::class . ':start_mic_feed');
    $app->post('/stop_cam_feed', HomeController::class . ':stop_cam_feed');
    $app->post('/stop_mic_feed', HomeController::class . ':stop_mic_feed');
    $app->post('/command', HomeController::class . ':command');
    $app->post('/signup', HomeController::class . ':signup');
};
