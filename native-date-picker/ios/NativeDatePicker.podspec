Pod::Spec.new do |s|
  s.name           = 'NativeDatePicker'
  s.version        = '1.0.0'
  s.summary        = 'Native date and time picker for iOS'
  s.description    = 'Presents a UIDatePicker in a bottom sheet and reports the date the user confirmed.'
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
