#!/usr/bin/env ruby
#
# Checks etcd node self stats
# ===
#
# DESCRIPTION:
#   This script pings the UDR service to trigger the data collection API
#
# OUTPUT:
#   plain-text
#
# PLATFORMS:
#   all
#
# DEPENDENCIES:
#   sensu-plugin Ruby gem
#   rest_client Ruby gem
#

require 'rubygems' if RUBY_VERSION < '1.9.0'
require 'sensu-plugin/check/cli'
require 'rest-client'
require 'json'

class PingUDRService < Sensu::Plugin::Check::CLI
  def run
    begin
      r = RestClient::Resource.new("http://localhost:8080/udr/api", :timeout => 60 ).get
      if r.code == 200
        ok "UDR service ping was successfull"
      else
        critical "Oops!"
      end
    end
  end
end
