Pod::Spec.new do |s|
  s.name           = 'DeviceHelper'
  s.version        = '1.0.0'
  s.summary        = 'Device and hardware information for iOS'
  s.description    = 'Reads the OS version, device model, memory and battery state, and reports battery changes.'
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
