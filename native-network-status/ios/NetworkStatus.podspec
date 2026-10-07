Pod::Spec.new do |s|
  s.name           = 'NetworkStatus'
  s.version        = '1.0.0'
  s.summary        = 'Network connection status for iOS'
  s.description    = 'Reports whether the device is connected, over which kind of network, and notifies when that changes.'
  s.author         = 'DVMS'
  s.homepage       = 'https://docs.expo.dev/modules/'
  s.platforms      = { :ios => '15.1' }
  s.swift_version  = '5.9'
  s.source         = { git: '' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES' }

  s.source_files = '**/*.{h,m,mm,swift}'
end
