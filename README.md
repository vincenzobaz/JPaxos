JPaxos /custom-leader/
===============

Description
-----------

JPaxos is a Java library and runtime system for efficient state machine
replication. With JPaxos it is very easy to make a user-provided service
tolerant to machine crashes. Our system supports the crash-recovery model of
failure and tolerates message loss and communication delays.

You are free to use JPaxos as an experimental platform for research into
software-based replication, or as a library for your commercial products,
provided that the LGPL3.0 licence is respected (see the LICENCE file).

Our system implementation is based on solid theoretical foundations, following
the state-of-the-art in group communication research. We intend to publish some
of the scientific results of the JPaxos project in the future.

`custom-leader`
-------
This branch contains a fork of the original JPaxos implementation aiming
to study the impact of a new leader election algorithm.
Refer to [this report](https://github.com/vincenzobaz/JPaxos-Benchmark/blob/master/paper/main.pdf)
for more information.

Developer resources
-------------------

Documentation: http://www.it-soa.eu/jpaxos

Original repository: https://github.com/JPaxos/JPaxos


License
-------

This software is distributed under the LGPL licence; for license details please
read the LICENCE file.
