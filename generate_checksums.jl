using Pkg

Pkg.add("MD5")

using SHA
using MD5

function write_hash(path, hash_function)
  hash_path = path * "." * String(Symbol(hash_function))
  if isfile(hash_path)
    return false
  end
  open(hash_path, "w") do io
    write(io, bytes2hex(open(hash_function, path)))
  end
  if (!endswith(path, "-local.xml"))
    run(`git add $hash_path`)
  end

  return true
end

for (root, dir, files) in walkdir("thedarkcolour")
  for file_name in files
    if (endswith(file_name, ".jar") || endswith(file_name, ".pom") || endswith(file_name, ".xml"))
      path = joinpath(root, file_name)
      if (write_hash(path, md5))
        println("Generated MD5 hash for $file_name")
      end
      if (write_hash(path, sha1))
        println("Generated SHA1 hash for $file_name")
      end
    end
  end
end

print("All hashes up-to-date")