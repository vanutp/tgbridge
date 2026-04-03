cd ($env | get TGBRIDGE_ROOT | default $env.FILE_PWD)

let version = open gradle.properties | parse "{key}={value}" | where key == "projectVersion" | get 0 | get value

let paper_release = $"build/release/tgbridge-($version)-paper.jar"
for path in (ls test-servers/paper | where type == "dir" | get name) {
  glob $"($path)/plugins/tgbridge-*.jar" | each {|x| rm $x }
  cp $paper_release $"($path)/plugins"
}
let fabric_release = $"build/release/tgbridge-($version)-fabric.jar"
for path in (ls test-servers/fabric | where type == "dir" | get name) {
  glob $"($path)/mods/tgbridge-*.jar" | each {|x| rm $x }
  cp $fabric_release $"($path)/mods"
}
let fabric_obf_release = $"build/release/tgbridge-($version)-fabric-obf.jar"
for path in (ls test-servers/fabric-obf | where type == "dir" | get name) {
  glob $"($path)/mods/tgbridge-*.jar" | each {|x| rm $x }
  cp $fabric_obf_release $"($path)/mods"
}
let forge_releases = {
  "16.5": $"build/release/tgbridge-($version)-forge-1.16.5.jar",
  "19.2": $"build/release/tgbridge-($version)-forge-1.19.2.jar",
  "20.1": $"build/release/tgbridge-($version)-forge-1.20.1.jar",
  "21.1": $"build/release/tgbridge-($version)-neoforge-1.21.jar",
}
for path in (ls test-servers/forge | where type == "dir" | get name) {
  let instance_version = $path | split row '/' | last | split row '-' | get 0
  glob $"($path)/mods/tgbridge-*.jar" | each {|x| rm $x }
  cp ($forge_releases | get $instance_version) $"($path)/mods"
}
