$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$javaCommand = Get-Command javac -ErrorAction SilentlyContinue
if (-not $javaCommand -and $env:JAVA_HOME) {
    $javaHomePath = $env:JAVA_HOME
    if ((Split-Path $javaHomePath -Leaf) -eq 'bin') { $javaHomePath = Split-Path $javaHomePath }
    $javaCommand = Get-Item -LiteralPath (Join-Path $javaHomePath 'bin/javac.exe') -ErrorAction SilentlyContinue
}
if (-not $javaCommand) { throw 'Instale un JDK 17 o superior y configure JAVA_HOME o el PATH.' }
$compiler = if ($javaCommand.Source) { $javaCommand.Source } else { $javaCommand.FullName }
$runtime = Join-Path (Split-Path $compiler) 'java.exe'
New-Item -ItemType Directory -Force -Path 'build/classes' | Out-Null
$sources = @(Get-ChildItem 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object FullName)
& $compiler -encoding UTF-8 --release 17 -d build/classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Error de compilación.' }
& $runtime '-Dfile.encoding=UTF-8' -cp build/classes com.udea.co.demo.Main
