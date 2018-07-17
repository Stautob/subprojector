#!/usr/bin/env bash

function renameFileAndCreateDirectoriesIfNeccessary {
  local OldPath="$1"
  local ScriptDir="$2"
  local OldProjectDomainString="$3"
  local NewProjectDomainString="$4"
  local OldProjectDomainAsPath="${3//\.//}"
  local NewProjectDomainAsPath="${4//\.//}"
  local EscapedOldProjectDomainString=${OldProjectDomainString//\./\\.}
  local OldPathWithProjectDomainStringReplaced="${OldPath//$EscapedOldProjectDomainString/${NewProjectDomainString}}"
  local NewPath="${OldPathWithProjectDomainStringReplaced//${OldProjectDomainAsPath}/${NewProjectDomainAsPath}}"
  if [ $OldPath != $NewPath ]; then
    echo -e "Moving file: ${OldPath/$ScriptDir/.}"
    echo -e " \t to: ${NewPath/$ScriptDir/.}"
    echo
    local targetDir="$(dirname "$NewPath")"
    local sourceDir="$(dirname "$OldPath")"
    mkdir -p $targetDir
    mv $OldPath $NewPath
  fi
}

export -f renameFileAndCreateDirectoriesIfNeccessary;

function capitalizeFirstLetterLowerAllElse {
  echo $1 | awk '{print toupper(substr($0,0,1))tolower(substr($0,2))}'
}

function autoSedForPlatform {
  local orig=$1
  local replacement=$2
  shift 2
  if [[ "$platform" == 'linux' ]]; then
    sed -i "s/$orig/$replacement/g" $@
  elif [[ "$platform" == 'OSX' ]]; then
    sed -i '' -e "s/$orig/$replacement/g" $@
  fi
}

export -f autoSedForPlatform;

green="\e[32m"
blue="\e[34m"
noFormat="\e[0m"

platform='unknown'
unamestr=`uname`
if [[ "$unamestr" == 'Linux' ]]; then
  platform='linux'
elif [[ "$unamestr" == 'Darwin' ]]; then
  platform='OSX'
else
  echo -e "Unknown Platform. Exiting now."
  exit 
fi

echo -e "Detected platform: $platform"
export platform

# Get directory of script
ScriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Last project domain
OldProjectDomainString="ch.stautob.eclipse.subprojector"

# Array containing the segments of the domain
IFS='.' read -ra OldProjectDomain <<< "$OldProjectDomainString"

# Prompt for new project domain
echo -e "Current project name is: $OldProjectDomainString"
echo -e "Enter the new project domain (i.e. com.cevelop.pluginname):"
# Reading the new project domain from the console
read NewProjectDomainString

# Array containing the segments of the new domain
IFS='.' read -ra NewProjectDomain <<< "$NewProjectDomainString"

# Replace all occurences of OldProjectDomainString in files
echo "---------------------------------"
echo "Changing project name inside files"
echo "---------------------------------"

# Replacing full domain
grep --exclude="setup.sh" --exclude-dir=".git" -rl -- "$OldProjectDomainString" ${ScriptDir} | xargs bash -c 'autoSedForPlatform $@' $0 ${OldProjectDomainString//\./\\.} ${NewProjectDomainString}
# Replacing only project name
OldPluginName=`capitalizeFirstLetterLowerAllElse ${OldProjectDomain[${#OldProjectDomain[@]}-1]}`
NewPluginName=`capitalizeFirstLetterLowerAllElse ${NewProjectDomain[${#NewProjectDomain[@]}-1]}`

grep --include "\*.MF" --include ".project" --include ".gitlab-ci.yml" --exclude-dir=".git" -rl -- "$OldPluginName" ${ScriptDir} | xargs bash -c 'autoSedForPlatform $@' $0 $OldPluginName $NewPluginName

# Move files
echo "---------------------------------"
echo "Moving files"
echo "---------------------------------"

find ${ScriptDir}/*/ -type f -exec bash -c 'renameFileAndCreateDirectoriesIfNeccessary "$@"' $0 {} ${ScriptDir} ${OldProjectDomainString} ${NewProjectDomainString} \;
# Replace OldProjectDomainString in this script for the next execution
autoSedForPlatform "OldProjectDomainString=\"$OldProjectDomainString\"$" "OldProjectDomainString=\"$NewProjectDomainString\"" "${ScriptDir}/setup.sh"

echo "---------------------------------"
echo "Renaming ProjectFolder"
echo "---------------------------------"

mv "${ScriptDir}/${OldPluginName}Project/" "${ScriptDir}/${NewPluginName}Project/"

# Clean up
echo "---------------------------------"
echo "Cleaning up"
echo "---------------------------------"

find ${ScriptDir} -type d -empty -delete

echo
echo  "Successfully renamed project from $OldProjectDomainString to $NewProjectDomainString"


