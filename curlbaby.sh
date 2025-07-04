 
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"
 
mkdir -p curlbaby/target/classes
mkdir -p curlbaby/lib
 
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java to run cUrlBaby"
    exit 1
fi 

if [ ! -f "curlbaby/lib/json-simple-1.1.1.jar" ]; then 
    for backup_path in "backup/client/lib" "../backup/client/lib" "lib"; do
        if [ -f "$backup_path/json-simple-1.1.1.jar" ]; then
            echo "Copying json-simple-1.1.1.jar from $backup_path"
            cp "$backup_path/json-simple-1.1.1.jar" curlbaby/lib/
            break
        fi
    done
     
    if [ ! -f "curlbaby/lib/json-simple-1.1.1.jar" ]; then
        echo "Warning: json-simple-1.1.1.jar not found. The application may not function correctly."
    fi
fi
 
JAVA_FILES=$(find com/curlbaby -name "*.java" 2>/dev/null)

if [ -z "$JAVA_FILES" ]; then
    echo "Error: No Java source files found in com/curlbaby directory"
    exit 1
fi

echo "Compiling cUrlBaby application..." 
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" || "$OSTYPE" == "cygwin" ]]; then
    CP_SEP=";"
else
    CP_SEP=":"
fi
 
javac -cp "curlbaby/lib/*" -d curlbaby/target/classes $JAVA_FILES

if [ $? -eq 0 ]; then
    echo "Compilation successful. Starting cUrlBaby application..."
    echo ""
    java -Djava.awt.headless=true -cp "curlbaby/target/classes${CP_SEP}curlbaby/lib/*" com.curlbaby.CurlBabyApp "$@"
else
    echo "Compilation failed. Please fix the errors."
    exit 1
fi