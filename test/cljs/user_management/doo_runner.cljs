(ns user-management.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [user-management.core-test]))

(doo-tests 'user-management.core-test)

