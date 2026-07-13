(ns kitchen.governor
  "KitchenSupportGovernor — the independent safety/traceability
  layer named in this repository's README/business-model.md, gating
  the robot-dispensed physical work (dishwashing, prep-surface
  cleaning, supply restocking) an advisor may propose. The governor
  never dispatches hardware itself. Modeled on
  cloud-itonami-isco-4311's bookkeeping.governor. Task twist: a
  proposed dishwash cycle's measured sanitize temperature must fall
  inside the registered food-safety band — sanitizing water
  temperature is a food-safety spec, not a feel test — and a
  proposed restock quantity is arithmetic comparison against the
  registered ceiling — over-ordering beyond registered storage
  capacity is a storage risk, not thrift.

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. client provenance — the organization must be registered.
    2. no-actuation      — proposal :effect must be :propose (the
                           governor never dispatches hardware; it only
                           gates what the robot may execute).
    3. kitchen basis        — a task approval must cite a REGISTERED
                           kitchen belonging to this client.
    4. sanitize-temperature band — the proposed measured sanitize
                           temperature must fall inside the kitchen's
                           registered [:min-sanitize-temp-c,
                           :max-sanitize-temp-c] band (a food-safety
                           spec, not a feel test).
    5. restock ceiling      — the proposed restock quantity must not
                           exceed the kitchen's registered
                           :max-restock-quantity (a storage risk, not
                           thrift).
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off per
  business-model.md's Trust Controls — these are :high/
  :safety-critical regardless of confidence):
    6. :op :approve-hot-surface-proximity (no robot operation near
                           hot surfaces/open flames without the
                           governor gate).
    7. :op :approve-sharp-tool-zone-entry (sharp-tool zones require
                           human sign-off).
    8. low confidence (< `confidence-floor`)."
  (:require [kitchen.store :as store]))

(def confidence-floor 0.6)

(def ^:private always-escalate-ops #{:approve-hot-surface-proximity
                                     :approve-sharp-tool-zone-entry})

(defn- hard-violations [{:keys [request proposal]} client-record k]
  (let [{:keys [op sanitize-temp-c restock-quantity]} proposal
        task? (= :approve-kitchen-task op)]
    (cond-> []
      (nil? client-record)
      (conj {:rule :no-client :detail "未登録 client"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation :detail "effect は :propose のみ許可（governor はハードウェアを直接起動しない）"})

      (and task? (nil? k))
      (conj {:rule :unknown-kitchen :detail "未登録 kitchen への作業承認は不可"})

      (and task? k (not= (:client-id k) (:client-id request)))
      (conj {:rule :kitchen-wrong-client :detail "kitchen が別 client のもの"})

      (and task? k (number? sanitize-temp-c)
           (or (< sanitize-temp-c (:min-sanitize-temp-c k))
               (> sanitize-temp-c (:max-sanitize-temp-c k))))
      (conj {:rule :sanitize-temp-out-of-band
             :detail (str "測定殺菌温度 " sanitize-temp-c "℃ が登録済み食品安全帯域 ["
                          (:min-sanitize-temp-c k) ", " (:max-sanitize-temp-c k)
                          "]℃ の外（殺菌水温は食品安全仕様であって感覚テストではない）")})

      (and task? k (number? restock-quantity) (> restock-quantity (:max-restock-quantity k)))
      (conj {:rule :restock-exceeds-ceiling
             :detail (str "補充数量 " restock-quantity " > 登録済み上限 "
                          (:max-restock-quantity k) "（過剰発注は在庫リスクであって倹約ではない）")}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `kitchen.store/Store`. Pure — never mutates the
  store, never dispatches the robot."
  [request context proposal store]
  (let [client-record (store/client store (:client-id request))
        k (some->> (:kitchen-id proposal) (store/kitchen store))
        hard (hard-violations {:request request :proposal proposal}
                              client-record k)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        always-risky? (contains? always-escalate-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not always-risky?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky?))}))
