{pkgs ? import <nixpkgs> {}}:
pkgs.mkShell {
  packages = [
    (pkgs.writers.writeNuBin "setup-mc" {
      makeWrapperArgs = [
        "--set"
        "TGBRIDGE_ROOT"
        (builtins.toString ./.)
      ];
    } (builtins.readFile ./setup-mc.nu))
  ];
  JAVA_HOME = pkgs.temurin-bin-25;
}
