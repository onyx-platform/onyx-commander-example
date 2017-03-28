# onyx-commander-example

### Usage

- Spin up ZooKeeper and Kafka with Docker compose: `docker-compose -f docker-compose-single-broker.yml up`
- Open the test under `onyx-commander-example/test/onyx_commander_example/jobs/commander_test.clj`
- If your Docker IP isn't `127.0.0.1`, change the ZooKeeper and Kafka hosts at the top of the file
- Run the test

### Acknowledgments

Distributed Masonry would like to thank Day8 for funding this example.

### License

Copyright © 2017 Distributed Masonry

Distributed under the Eclipse Public License, the same as Clojure.
