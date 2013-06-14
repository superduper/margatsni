Margatsni P2P Socket.IO client
==============================

Directory structure
~~~~~~~~~~~~~~~~~~~

(root)
 ----
  |-- lib                         [ unmanaged jar dependencies] 
  |-- margatsni-bootstrap-server  [ bootstrap server subproject]
  |-- margatsni-nodes             [ node subproject ]
  |-- docs                        [ rare docs ]
  |-- project                     [ keeps SBT related settings ]


Prerequisites
~~~~~~~~~~~~~

Scala 2.10, SBT 0.12


How to compile
~~~~~~~~~~~~~~

Run this in your console:  
```
    cd /path/to/margatsni-scala-project-root
    make clean dist
```

Will place fresh compiled jars into ``dist/bin`` 

