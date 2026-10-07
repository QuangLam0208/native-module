Pod::Spec.new do |s|
  s.name           = 'NativeToast'
  s.version        = '1.0.0'
  s.summary        = 'Native toast for iOS'
  s.description    = 'Shows a short, auto-dismissing toast on the key window.'
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
