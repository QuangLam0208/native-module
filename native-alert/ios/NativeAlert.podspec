Pod::Spec.new do |s|
  s.name           = 'NativeAlert'
  s.version        = '1.0.0'
  s.summary        = 'Native alert with two or three buttons for iOS'
  s.description    = 'Presents a UIAlertController with confirm, optional neutral and cancel buttons and reports which one was tapped.'
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
