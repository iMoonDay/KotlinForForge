#=
update_maven_local:
- Julia version: 1.8.4
- Author: thedarkcolour
- Date: 2023-01-02
=##
# Publish to maven local within this project
original_pwd = pwd()
println("Fetching from upstream...")
# Sync maven-metadata-local.xml with upstream
run(`git fetch`)
println("Syncing local Maven metadata...")
for (root, dir, files) in walkdir("thedarkcolour")
  for file_name in files
    if (file_name == "maven-metadata.xml")
      file = joinpath(root, file_name)
      local_file = replace(file, ".xml" => "-local.xml")

      if (isfile(local_file) || mtime(local_file) < mtime(file))
        cp(file, local_file, force=true)
      end
    end
  end
end


# Compiles KFF and publishes it to local Maven within this folder
publish_special_maven() = run(`gradlew.bat -Dmaven.repo.local=$original_pwd publishAllMavens`)
# Executes the Gradle task when specified project folder is in same folder as this website folder
cd(publish_special_maven, "../" * ARGS[1])

for (root, dir, files) in walkdir("thedarkcolour")
  for file_name in files
    file = joinpath(root, file_name)

    if (endswith(file_name, "local.xml"))
      cp(file, replace(file, "-local.xml" => ".xml"), force=true)
    elseif (!contains(file_name, "local.xml"))
      run(`git add $file`)
    end
  end
end

# Regenerate hashes
include("generate_checksums.jl")

# todo update the website HTMLs
