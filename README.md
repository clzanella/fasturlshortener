Encurtador de URLs
===========================

Criar um serviço capaz de encurtar e atender requisições em urls encurtadas (redirecionamento)

Feito conforme [estes requisitos](https://github.com/ledbruno/desafios/tree/master/1%20-%20Easy/Encurtador%20de%20URL).

## Build
Package maneger utilizado foi o Gradle:

- Build.
``` 
gradle build 
```
- Jar executavel. 
``` 
gradle allInOneJar 
```
- Executar.
``` 
java -jar build/libs/app.jar 
```

#### Detalhes da implementaçao
- View basica para encurtar URLs `http://localhost:8080`
- Rota POST para encurtar URLs `http://localhost:8080/shortener`
``` 
# Body request
{ 
  url: "google.com",
}
# Response
{ 
  newUrl: "http://localhost:8081/abc123ab",
  createdAt: "3423423424"
} 
```
- Rota GET para acessar URLs encurtada
GET  `http://localhost:8081/abc123ab` => GET `http://www.zambas.com.br`

#### Detalhes para ganho de performance
Precisei alterar a Stack (Spring) solicitada para efetuar as seguintes otimizaçoes:
- Eliminar a carga de componentes (via annotations) e passar a utilizar um microframework no estilo Sinatra (Jooby)
- Utilizar IoC (inversão de controle (testabilidade)) mas sem injeção de dependência
- Substituir o server por um com modelo de execuçao assincrona (trocar o Jetty por Netty ou Undertow)
- Armazenamento no banco simplificado (perdendo dados ex: createdAt e o parâmetro de expiração)
- eliminar o ORM, pois o modelo de dados é simples
- ConnectionPool (HikaryCP)
- Configurar o ThreadPool conforme o numero de Processadores
- Minimizado o uso de refletion (SQL escrito na mao, parse do resultado no braco, serializaçao e deserializaçao JSON feita manualmente nos controllers )


## Executar testes de performance

Para testes de performance foi utilizada a ferramenta Blazemeter Taurus, que por baixo usa o JMeter porem uma notacao yaml com legibilidade excelente.
- Para instalar o Taurus:
``` 
sudo apt update
sudo apt install python-pip
sudo pip install bzt --upgrade
```
- Para executar o servidor:
```
java -Xms1024m -Xmx1024m -jar build/libs/app.jar 
```
- Para executar o teste:
``` 
cd src/test/resources/performance/load-test
bzt test.yml
```
