SquashFs Tools
==============

To convert a Docker tar.gz layer to squashfs:

```
java -cp target/squashfs-tools-1.0.0-SNAPSHOT-executable.jar org.apache.hadoop.squashfs.tools.SquashConvert <tar-gz-file> <squashfs-file>

```

To dump the raw content of a squashfs file:

```
java -cp target/squashfs-tools-1.0.0-SNAPSHOT-executable.jar org.apache.hadoop.squashfs.tools.SquashFsck <squashfs-file>
```

