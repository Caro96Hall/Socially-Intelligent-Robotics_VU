#!/usr/bin/env bash
cd /var/www/default
composer install --no-dev --no-progress --prefer-dist --optimize-autoloader
