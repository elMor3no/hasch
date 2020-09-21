(ns hasch.api-test
  (:require [hasch.core :refer [edn-hash uuid squuid b64-hash]]
            [hasch.benc :refer [xor-hashes]]
            [hasch.md5 :as md5]
            [hasch.hex :as hex]
            [hasch.platform :refer [uuid5 sha512-message-digest hash->str #?(:cljs utf8)]]
            [incognito.base :as ic]
            [clojure.test :as t :refer [is deftest testing]]))

#?(:cljs (def byte-array into-array))


(defrecord Bar [name])

(deftest hash-test
  (testing "Basic hash coercions of EDN primitives."
    (is (= (edn-hash nil)
           '(184 36 77 2 137 129 214 147 175 123 69 106 248 239 164 202 214 61 40 46 25 255 20 148 44 36 110 80 217 53 29 34 112 74 128 42 113 195 88 11 99 112 222 76 235 41 60 50 74 132 35 52 37 87 212 229 195 132 56 240 227 105 16 238)))

    (is (= (edn-hash true)
           '(221 223 252 44 103 48 51 199 71 184 156 187 201 140 35 99 235 153 185 70 157 229 122 4 111 90 12 150 43 67 185 166 210 79 54 62 117 173 76 252 187 67 163 85 202 124 63 252 109 44 47 70 74 129 52 241 35 15 116 253 241 141 50 131)))

    (is (= (edn-hash false)
           '(54 0 110 63 158 137 176 89 220 235 107 213 84 159 27 25 148 206 193 96 192 73 41 255 220 181 215 106 208 220 173 69 213 190 181 70 141 193 1 225 188 142 127 176 102 61 13 54 151 161 195 158 152 190 212 168 91 43 153 108 122 123 90 32)))

    (is (= (edn-hash \f)
           '(211 133 203 224 194 174 136 44 216 77 98 85 54 188 116 101 139 174 40 108 48 180 235 231 214 189 34 246 32 30 56 45 179 218 36 206 61 191 79 160 212 162 212 226 235 17 27 228 218 74 17 229 9 147 187 232 35 244 179 233 66 165 152 253)))

    (is (= (edn-hash \ä)
           '(51 232 113 238 243 104 216 10 143 88 143 111 122 220 35 138 251 22 8 130 238 73 253 62 143 207 208 45 116 21 120 18 253 34 160 30 144 46 182 7 160 254 197 120 199 220 140 209 3 66 25 214 131 145 17 222 28 157 22 103 226 254 178 186)))

    (is (= (edn-hash "hello")
           '(178 114 9 243 3 150 0 132 236 216 60 87 108 34 2 35 85 37 203 202 97 176 9 55 25 191 143 251 251 47 49 139 99 191 77 63 167 158 61 183 233 59 43 57 16 252 121 198 65 201 112 167 96 61 134 122 177 149 45 87 233 23 173 192)))

    (is (= (edn-hash "小鳩ちゃんかわいいなぁ")
           '(2 191 84 39 34 44 227 102 135 109 17 136 159 80 253 7 40 0 170 134 198 204 137 10 194 21 113 203 2 87 125 80 172 165 111 110 222 7 123 138 148 124 207 180 240 207 91 6 248 28 53 168 143 30 106 103 101 82 133 215 69 35 93 47)))

    (is (= (edn-hash "😡😡😡")
           '(18 17 129 25 13 183 170 164 178 18 97 0 123 151 164 145 95 197 214 178 107 96 255 105 255 104 69 21 205 160 13 222 9 55 63 37 174 33 35 86 204 73 17 110 82 107 151 64 63 79 191 246 177 76 71 24 107 44 43 156 178 169 195 214)))

    (is (= (edn-hash (int 1234567890))
           (edn-hash (long 1234567890))
           #?(:clj (edn-hash (biginteger "1234567890")))
           #?(:clj (edn-hash (bigint "1234567890")))
           '(65 199 158 164 193 95 213 144 233 29 41 86 123 106 110 215 117 225 149 249 204 124 220 217 226 120 131 178 61 133 39 228 182 233 235 249 10 249 141 122 101 25 46 134 18 222 175 224 134 61 167 114 15 109 2 146 38 65 1 55 128 137 144 55)))

    (is (= (edn-hash (double 123.1))
           (edn-hash (float 123.1))
           '(155 181 33 252 126 113 188 20 210 155 50 24 125 212 205 160 135 108 90 43 154 65 61 229 226 83 11 110 64 61 124 45 43 186 152 127 64 171 171 154 28 149 180 136 229 69 195 145 126 99 56 14 48 194 180 126 212 83 123 206 36 189 189 167)
           #?(:clj (edn-hash (BigDecimal. "123.1")))))

    (is (= (edn-hash :core/test)
           '(62 51 214 78 41 84 37 205 69 197 105 26 235 55 30 87 46 117 187 194 101 184 139 244 111 232 98 175 16 174 182 211 11 171 154 64 90 18 229 93 188 246 33 234 102 145 68 30 92 0 81 208 210 10 124 137 203 18 249 138 226 253 60 62)))

    (is (= (edn-hash  #uuid "242525f1-8ed7-5979-9232-6992dd1e11e4")
           '(42 243 183 237 233 94 246 1 110 56 231 49 64 217 181 17 108 11 120 199 223 53 149 47 49 8 109 94 127 93 250 51 167 211 25 31 3 171 149 67 23 245 38 248 40 31 199 211 162 242 120 99 187 6 29 237 53 174 22 192 27 159 227 164)))

    (is (= (edn-hash (#?(:clj java.util.Date. :cljs js/Date.) 1000000000000))
           '(177 226 212 235 221 67 176 34 184 69 101 45 117 193 95 187 54 50 210 149 10 193 10 67 220 174 25 99 176 115 250 216 29 49 148 167 52 86 203 90 30 170 62 149 115 102 109 120 128 62 2 213 188 41 203 91 202 106 142 100 119 160 26 3)))

    (is (= (edn-hash 'core/+)
           '(164 63 64 77 190 144 72 80 34 36 254 237 101 99 57 114 54 44 195 22 255 11 242 114 99 87 99 135 103 73 164 183 20 192 184 54 183 244 192 151 88 96 55 204 73 156 73 92 154 8 248 205 119 157 34 112 202 51 52 169 162 61 91 235)))

    (is (= (edn-hash '(1 2 3))
           '(244 105 186 110 183 117 195 78 70 57 251 132 133 114 134 175 228 94 242 41 194 191 186 237 163 178 255 193 141 120 5 137 223 130 170 47 231 133 78 131 128 194 115 140 186 169 124 71 205 210 228 236 82 97 166 158 190 98 106 80 237 149 96 102)))

    (is (= (edn-hash [1 2 3 4])
           '(172 52 37 123 179 106 243 207 88 177 218 22 170 25 13 155 205 89 156 251 253 50 3 3 191 74 229 97 252 37 162 240 197 252 240 199 177 8 96 227 121 100 106 132 68 227 175 189 247 184 108 25 117 154 186 63 108 4 210 20 75 25 239 199)))

    (is (= (edn-hash {:a "hello"
                      :balloon "world"})
           '(135 204 255 206 109 55 248 198 218 226 173 91 27 244 68 34 108 207 62 12 114 49 69 90 22 44 155 178 212 188 139 50 217 200 63 207 14 112 179 94 202 96 196 139 202 154 214 211 182 97 31 139 49 153 203 233 240 223 154 161 78 131 159 102)))

    (is (= (edn-hash #{1 2 3 4})
           '(42 216 217 238 97 125 210 112 2 83 128 62 82 47 119 14 59 95 246 107 191 138 251 102 201 52 9 132 96 243 199 223 218 81 88 130 165 214 125 48 222 30 64 233 101 122 196 84 11 93 186 26 92 225 203 161 196 98 186 138 174 118 244 248)))


    (is (= (edn-hash (Bar. "hello"))
           (edn-hash (ic/incognito-reader {'hasch.api-test.Bar map->Bar}
                                          (ic/incognito-writer {} (Bar. "hello"))))
           (edn-hash (ic/map->IncognitoTaggedLiteral (ic/incognito-writer {} (Bar. "hello"))))
           (edn-hash (ic/map->IncognitoTaggedLiteral {:tag 'hasch.api_test.Bar
                                                      :value {:name "hello"}}))
           '(236 35 140 74 245 164 93 1 239 144 253 91 193 51 241 129 149 210 99 169 16 130 21 235 236 166 36 205 80 10 215 106 173 39 96 197 241 49 64 219 252 119 65 15 87 24 2 253 0 143 61 187 88 216 238 226 146 40 197 51 82 208 246 127)))

    (is (= (edn-hash #?(:cljs (js/Uint8Array. #js [1 2 3 42 149])
                        :clj (byte-array [1 2 3 42 149])))
           '(135 209 248 171 162 90 41 221 173 216 64 218 222 93 242 60 243 5 190 153 101 194 74 130 55 184 84 148 167 94 210 250 140 211 6 234 221 25 113 83 153 75 180 4 194 163 178 197 243 126 27 172 248 169 161 90 102 172 160 98 249 32 42 157)))))

(deftest padded-coercion
  (testing "Padded xor coercion for commutative collections."
    (is (= (map byte
                (xor-hashes (map byte-array
                                 [[0xa0 0x01 0xf3] [0x0c 0xf0 0x5f] [0x0a 0x30 0x07]])))
           (map byte (xor-hashes (map byte-array
                                      [[0x0a 0x30 0x07] [0x0c 0xf0 0x5f] [0xa0 0x01 0xf3]])))))))

(deftest code-hashing
  (testing "Code hashing."
    (is (= (-> '(fn fib [n]
                  (if (or (= n 0) (= n 1)) 1
                      (+ (fib (- n 1)) (fib (- n 2)))))
               edn-hash
               uuid5)
           #uuid "386eabb0-8adc-52a2-a715-5a74c9197646"))))

(deftest hash-stringification
  (testing "Stringification."
    (is (= (hash->str (range 256))
           "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff"))))

(deftest squuid-test
  (testing "Sequential UUID functionality."
    (is (= (subs (str (squuid (uuid [1 2 3]))) 8)
           "-5c15-555e-a1c8-6166a78fc808"))))

(deftest b64-hash-test
  (testing "Testing the base64 encoding of a hash."
    (is (= (b64-hash [1 2 3 {:key 5 :value 10}])
           "TREJlRrK211AASiqQMFG9RLFW0CPC/arrCxeaUj27Qho2USJU40T01uCdjUg/OMiPGttyL1ELPCrVXXhMIroRQ=="))))

(deftest test-md5
  (is (= (hex/encode (md5/str->md5 "geheimnis"))
         "525e92c6aa11544a2ab794f8921ecb0f")))

#?(:cljs
   (deftest utf8-test
     (is (= (js->clj (utf8 "小鳩ちゃんかわいいなぁ"))
            [229 176 143 233 179 169 227 129 161 227 130 131 227 130 147 227
             129 139 227 130 143 227 129 132 227 129 132 227 129 170 227 129 129]))))
