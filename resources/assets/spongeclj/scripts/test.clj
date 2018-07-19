(def cmd-a (cmd
             :executor #(do (println %1)
                            (println (:id %2)))
             :permission "testcmd.exec.child.a"
             :arguments [(string-arg "id")]
             :description "Just a child command A"))

(def cmd-b (cmd
             :executor (fn [src, args]
                         (do (println args)
                             (send-message src "cmd-b but from script!")))
             :permission "testcmd.exec.child.b"
             :arguments [(string-arg "id")]
             :description "Just a child command B"))

(def-cmd
  :aliases ["testscript" "tsts"]
  ;:executor test-fn
  :permission "testcmd.exec"
  :children {["a"]     cmd-a
             ["b" "v"] cmd-b}
  :description "Just test command"
  :extended-description "And it's extended description")