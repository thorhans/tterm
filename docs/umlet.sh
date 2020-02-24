# View ‘UML.uxf’ in UMLet.
# 
# - TODO This works when I double-click it, but not when I start it from
#   Bash???

echo "'umlet.sh' can't start from shell."
echo "Double click it in the Windows Explorer".
echo

# My current version of UMLet does not start with Oracle's Java 9, so
# I use my Android Studio's JRE 8.
# 
/d/prg/android/studio/jre/bin/java   \
    -Dsun.java2d.xrender=f           \
    -jar c:/prg/java/umlet/umlet.jar \
    -filename="UML.uxf"
