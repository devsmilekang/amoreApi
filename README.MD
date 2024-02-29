# 프로젝트 제목

메모리 캐시(in-memory cache) 구현 과제

## 실행 방법

아래의 단계를 따라 프로젝트를 실행할 수 있습니다.

### 1. 소스 코드 가져오기

프로젝트 소스 코드를 클론합니다.

```
git clone https://github.com/devsmilekang/amoreApi.git
```

### 2. 어플리케이션 실행

프로젝트 루트 디렉토리로 이동하여 gradlew를 사용하여 프로젝트를 실행합니다.

```
./gradlew bootRun
```

### 3. 웹 브라우저에서 확인

```
http://localhost:8080/swagger-ui.html  #swagger를 통해 API 목록 조회 및 호출할 수 있습니다.
http://localhost:8080/h2-console    #현재 메모리 DB에 있는 값을 확인할 수 있습니다.
```

## Usecases

### case 1.Query

* 클라이언트 코드는 Cache Service 가 제공하는 API 를 통하여, 카테고리 리스트를 조회할 수 있
  어야 한다.
  - GET 
    - http://localhost:8080/categories
    - http://localhost:8080/categories/names
* 특정 카테고리에 속한 상품 리스트를 조회할 수 있어야 한다.
  - GET
    - http://localhost:8080/categories/{categoryNo}/products
* 특정 상품에 대하여 상품명, 카테고리, 가격을 조회할 수 있어야 한다
  - GET
    - http://localhost:8080/products/{productsNo}

### case 2.Data Loading and Reloading

* 캐시는 초기 구동 시 데이터를 원본 데이터베이스로부터 loading 한다.
  - LoadingCacheConfig.java 에서 초기 구동시 캐시를 loading
* Cache Miss 가 발생하면 적절한 시점에 cache 는 스스로 해당 부분의 데이터를 원본 데이터베
  이스로부터 reloading 한다.
  - 각각의 Service에서 캐시를 조회 후 캐시에 데이터가 존재하지 않으면 캐시 추가
### case 3. Cache Data Eviction Policy
1. 만료된 캐시를 캐시 삽입시 삭제한다.
2. 캐시가 가득차서 더 넣을 곳이 없다면 호출횟수/(현재시간-생성시점[초])로 제일 작은 값을 제거한다.

  첫 번째는 캐시를 사용하는 사용자가 판단하여 캐시를 얼만큼 유지할 것인지 판단하여 등록한다.
   - 짧은 주기를 입력한 경우는 최신정보가 중요할 때 사용한다. (상품의 가격 등 변동과 영향이 심한 것)
   - 긴 주기를 입력한 경우는 변동성이 별로 없고 최신정보가 바로 교체되지 않아도 괜찮을 때 사용할 수 있다.
    이와 같이 캐시에 대하여 사용자가 판단하여 사용할 수 있도록 만료시간을 지정한다.

두 번째는 초당 호출횟수를 기준으로 가장 적게 사용된 것을 제거한다.
   - 사용이 빈번하지 않은 캐시를 제거하기 위함이다.
   - 한 번만 사용하였는데 캐시에 올라가있거나 오랜 시간이 지났지만 사용을 별로 하지 않았을 때 우선순위가 되기 위함이다.

### case 4. Cache Optimization (Cache Miss 의 처리 및 최소화 방법)
 - Cache Miss 를 최소화하기 위한 비즈니스 로직이나 알고리즘을 제안하고 코드로 구현하라
   - Cache Miss가 발생하면 비동기로 cache를 적재한다. 이로인해 cache를 적재하는 시간을 절약할 수 있다.
   - 조회가 순간 몰릴 것으로 예상되는 지점에 미리 캐시를 로드할 수 있도록 한다.
     - 상품조회의 경우 특정상품이 트래픽이 몰릴 경우를 대비하여 상품조회시 만료시간이 10초 남았고 Hit 수가 100이 넘는다면 미리 캐시를 로드(비동기)한다. (productService.findProductByNo)

### case 5. 원본 데이터에 대한 ADD / DELETE / UPDATE
* Note! Cache 자체의 기능은 아니며, 문제 풀이의 구현상 편의를 위하여 클라이언트가 데이터
베이스의 원본을 직접 변경하는 행위를 모사하는 API 를 Cache Service 에서 제공한다.
* (현실에서는 별도의 API 를 통하여 일어나는 행위이므로 Cache Service 입장에서는 실시간으로
인지할 수 없는 transaction 임)
* 특정 카테고리명을 변경할 수 있어야 한다.
  - PUT
    - http://localhost:8080/categories/{categoryNo}

* 특정 상품명을 변경할 수 있어야 한다.
  - PUT
    - http://localhost:8080/products/{productNo}
* 특정 상품의 가격을 변경할 수 있어야 한다.
  - PUT
    - http://localhost:8080/products/{productNo}
* 위의 동작들에 대하여 Cache 가 아닌 원본 데이터베이스의 내용을 직접 변경하여야 한다