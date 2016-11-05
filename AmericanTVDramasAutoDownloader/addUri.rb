#!/usr/bin/env ruby
require 'xmlrpc/client'
require 'pp'

client=XMLRPC::Client.new2("http://localhost:6800/rpc")
if ARGV.length == 1
        options={}
        url=ARGV[0]
elsif ARGV.length == 2
        options={ "dir" => ARGV[0] }
        url=ARGV[1]
end
if url.length > 0
        result=client.call("aria2.addUri", [ url ], options)
        #puts "#{url}"
        puts "#{result}"
end

