#!/bin/bash

# This runs as root on the server

chef_binary=`which chef-solo`
knife_binary=`which knife`

# Are we on a vanilla system?
if [ ! "$chef_binary" ] ; then
    # export DEBIAN_FRONTEND=noninteractive
	export CURRENT_USER=${CURRENT_USER:-"jenkins"}	
	# Upgrade headlessly (this is only safe-ish on vanilla systems)	
    apt-get update -qq
    
    # forces update of all installed packages
    apt-get -o Dpkg::Options::="--force-confnew" --force-yes -fuy dist-upgrade
    apt-get install -f -y -qq --force-yes git
    						
	# Install Ruby and Chef
    apt-get install -f -y -qq --force-yes ruby1.9.1 ruby1.9.1-dev make
    gem1.9.1 install --no-rdoc --no-ri chef --version 0.10.10 # 0.10.10 got problems with python vitualenv
	gem1.9.1 install --no-rdoc --no-ri knife-github-cookbooks
	# Install updated version of knife-github-cookbook
	git clone git://github.com/gbatalski/knife-github-cookbooks.git
	cd knife-github-cookbooks
	gem1.9.1 build *.gemspec
	gem1.9.1 install --no-rdoc --no-ri *.gem
	cd ..
	# done !#
	
    
    HOME_DIR=${HOME_DIR:-"`getent passwd $CURRENT_USER | cut -d: -f6`"}
    
    SOLO_DIR=$HOME_DIR/chef-solo
    
    mkdir $SOLO_DIR
    chown -R $CURRENT_USER $SOLO_DIR
    chmod -R 774 $SOLO_DIR
    chmod -R +s $SOLO_DIR
    cd $SOLO_DIR
    
	# Data bags
    mkdir $SOLO_DIR/data-bags
	# cache
	mkdir $SOLO_DIR/cache     
	# git clone our cookbooks
    mkdir $SOLO_DIR/cookbooks
    # git clone original cookbooks
    mkdir $SOLO_DIR/orig-cookbooks
    cd $SOLO_DIR/orig-cookbooks 
    git init 
    touch .gitignore
    git config --system user.name "$CURRENT_USER" 
    git config --system user.email "$CURRENT_USER@localhost"
    git add . 
    git commit -am"Download cookbook repository created"
    cd ..
    cd $SOLO_DIR/cookbooks 
    git init 
    touch .gitignore
    git add . 
    git commit -am"Github cookbook repository created"
    cd ..
    
	# clone all with submodules e.g. recursive
    #git clone --recursive git://github.com/gbatalski/cookbooks.git cookbooks
    
fi