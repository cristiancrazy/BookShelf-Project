# Libraries
!include MUI2.nsh


# =========== Installer information ===========
Name "BookShelf"
OutFile "BookShelf-Installer.exe"
!define MUI_ICON "icon.ico"

InstallDir "C:\Program Files\BookShelf" ; Default installer folder
BrandingText "Progetto indipendente - Cristian Capraro"
Unicode true

# =========== Pages ===========
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "MIT.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
  
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# =========== Interface ===========
!define MUI_ABORTWARNING
# =========== Lang ===========
!insertmacro MUI_LANGUAGE Italian

# =========== Install Section ===========
Section "BookShelf V. 1.0" BookShelf
    SetOutPath "$INSTDIR"
    File "BookShelf-1.0.jar"
    WriteUninstaller "$INSTDIR\Uninstall.exe" ; Create unistaller
    SetOutPath "$desktop"
    File "jre.msi"

    CreateShortCut "$desktop\BookShelf.lnk" "$INSTDIR\BookShelf-1.0.jar"
SectionEnd

Section "Uninstall"
    Delete "$INSTDIR\Uninstall.exe"
    Delete "$INSTDIR\BookShelf-1.0.jar"
    RMDir "$INSTDIR"
SectionEnd

LangString DESC_BookShelf ${LANG_ITALIAN} "Programma principale"

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT ${BookShelf} $(DESC_BookShelf)
!insertmacro MUI_FUNCTION_DESCRIPTION_END