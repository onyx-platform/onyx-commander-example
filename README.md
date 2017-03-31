# onyx-commander-example

This project is an example of using the [Commander pattern](https://www.youtube.com/watch?v=B1-gS0oEtYc) with Onyx.
We read commands from a Kafka topic, process them with Onyx, and produce materialized views using Datomic.
Commands generate read-receipts named events, which are fed back into Kafka to provide a point of synchronicity
for the initial caller of a command.

### Usage

- Spin up ZooKeeper and Kafka with Docker compose: `docker-compose up`
- Open the test under `onyx-commander-example/test/onyx_commander_example/jobs/commander_test.clj`
- If your Docker IP isn't `127.0.0.1`, change the ZooKeeper and Kafka hosts at the top of the file
- Run the test with `lein test`

### Acknowledgments

Distributed Masonry would like to thank Day8 for funding this example.

### License

Copyright © 2017 Distributed Masonry

Distributed under the Eclipse Public License, the same as Clojure.
