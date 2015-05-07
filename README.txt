##HPPC-RT: High Performance Primitive Collections "RealTime"

This is a fork of HPPC from CarrotSearch, see JavaDoc and documentation at: https://github.com/carrotsearch/hppc/wiki for more info.

IMPORTANT: Developers - read INSTALL.txt file for proper Eclipse setup and requirements.

---------------------------------------

This fork intend to follow the original HPPC as close as possible while 
adding, tweaking, and even modifiying some features, for the goal of improving HPPC realtime behaviour. 
Most of the internal modifications were done to effectively remove any temporary Objects creation at runtime, provided 
the containers are correctly pre-sized initially. 

###List of changes:
* Tweaked internal implementation to remove any allocation of temporary Objects at runtime.

* Ability to pre-allocate any container with a strong guarantee to neither reallocate, nor generating garbage as long as the container 
have less than capacity() elements.

* Pooled, recyclable iterators: ability to use iterators the usual way, without creating iterator instances
dynamically at runtime. 
That means in particular using the enhanced for loop without any dynamic allocation underneath.

* Primitive and Objects in-place array sorts, similar to java.util.Arrays with Comparable and Comparator versions.

* [Object|primitives]Arrays as a complement of java.util.Arrays for in-place buffer manipulation : reverse, rotate...etc. 

* Native and optimized in-place sort methods for ArrayLists, LinkedLists, ArrayDeques, Stacks.

* Custom hashing strategies to override the usual equals()/hashCode() in Hash containers, in specific CustomHash containers.

* Heaps : Priority queues, and Indexed (a.k.a indirect) priority queues.

* Double linked-lists, supporting all operations common to lists and dequeues, with rich bi-derectional iterating methods.

* Custom default values for methods returning default values in some circumstances. (see Javadoc)

* Boolean versions of containers when it makes sense: lists, queues, stacks, values in Maps.

...and some general performance and API tweaks.

All these new features are heavily Unit tested, so should work well.

Binaries are provided in 2 flavors : one compatible with Java 1.5, the other with Java 1.6 and beyond.
Both binaries have no external dependencies, and offer the same API.

Stable version is available on Maven:
````
<dependency>
    <groupId>com.github.vsonnier</groupId>
    <artifactId>hppcrt</artifactId>
    <version>0.6.8</version>
</dependency>
````



