(ns kitchen.store
  "SSoT for the ISCO-08 9412 independent kitchen support practice
  actor (itonami actor pattern, ADR-2607011000 / CLAUDE.md Actors
  section; README's 'Robotics premise' — a kitchen-support robot
  performs dishwashing, prep-surface cleaning and supply restocking
  under this advisor/governor pair, which never dispatches hardware
  itself). Modeled on cloud-itonami-isco-4311's bookkeeping.store.

  Domain:

    client  — a registered organization (:client-id, :name)
    kitchen — a registered kitchen {:kitchen-id :client-id :name
              :min-sanitize-temp-c number :max-sanitize-temp-c number
              :max-restock-quantity number}.
              `:min-sanitize-temp-c`/`:max-sanitize-temp-c` is the
              registered food-safety band a proposed dishwash cycle's
              measured sanitize temperature must fall inside —
              sanitizing water temperature is a food-safety spec, not
              a feel test; `:max-restock-quantity` is the registered
              ceiling a proposed restock order must not exceed —
              over-ordering beyond registered storage capacity is a
              storage risk, not thrift.
    record  — a committed operating record (approved kitchen task) —
              written ONLY via commit-record!.
    ledger  — append-only audit trail, commit or hold."
  )

(defprotocol Store
  (client [s client-id])
  (kitchen [s kitchen-id])
  (records-of [s client-id])
  (ledger [s])
  (register-client! [s client])
  (register-kitchen! [s k])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (client [_ client-id] (get-in @a [:clients client-id]))
  (kitchen [_ kitchen-id] (get-in @a [:kitchens kitchen-id]))
  (records-of [_ client-id] (filter #(= client-id (:client-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-client! [s client]
    (swap! a assoc-in [:clients (:client-id client)] client) s)
  (register-kitchen! [s k]
    (swap! a assoc-in [:kitchens (:kitchen-id k)] k) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:clients {} :kitchens {} :records [] :ledger []}
                                   seed)))))
