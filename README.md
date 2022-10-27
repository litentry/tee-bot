# TEE-bot

microservice within TEE

# Compile and Run as a common Java instance

## Compile

```bash
 ./mvnw clean package -Dmaven.test.skip=true
```

## Run

```bash

```

# Compile and Run as TEE hosted Java instance

## Install Occulum and Setup Occulum toolchains

TBD

## Compile

TBD

## Run within TEE

TBD

# Interfaces

## Discord

### Discord Messages Verification

Request: POST /discord/verify

```json
{
 "handler": "xyz#0123"
}
```

Response:

```json
{
    "data": {
        "handler": "xyz#0123",
        "guildId": ,
        "channleId": ,
        "msgId": ,
        "userId": ,
        "msg": "verification text",
        "msgCreatedAt": "2022-09-01T10:03:34.937018Z",
        "url": "https://discordapp.com/api/channels/{}/messages/{}",
        "authorization": ""
    },
    "message": "Discord verify message found",
    "hasErrors": false,
    "msgCode": 100101,
    "success": true
}
```

Fetch message contents from Discord directly:

```bash
curl "https://discordapp.com/api/channels/{}/messages/{}" -H 'authorization: {}'
```

Response:

```json
{
 "id": "",
 "type": 0,
 "content": "verification text",
 "channel_id": "",
 "author": {
  "id": "",
  "username": "xyz",
  "avatar": "",
  "avatar_decoration": null,
  "discriminator": "0123",
  "public_flags": 0
 },
 "attachments": [],
 "embeds": [],
 "mentions": [],
 "mention_roles": [],
 "pinned": false,
 "mention_everyone": false,
 "tts": false,
 "timestamp": "2022-09-02T10:03:34.885000+00:00",
 "edited_timestamp": null,
 "flags": 0,
 "components": []
}
```

### Check User joined Discord Guild

Check whether the user {handler} has joined {guildid} or not.

Notice: The {handler} must be in the URL-Encode format.

Request: GET /discord/joined?guildid={guildid}&handler={handler}

Response:

```json
{
 "data": true,
 "message": "Success",
 "hasErrors": false,
 "msgCode": 100000,
 "success": true
}
```

### Assign ID-Hubber Role to User

Assign the 'ID-Hubber' Role to the user {handler} who has joined {guildid}.

Notice: The {handler} must be in the URL-Encode format.

Request: GET /discord/assgin/idhubber?handler={}&guildid={}

Response:

```json
{
    "data": true,
    "message": "Success",
    "hasErrors": false,
    "msgCode": 100000,
    "success": true
}
```

### Has commented in Channel 'id-hubber' with Role 'ID-Hubber' in guild

Check whether the user {handler} with the 'ID-Hubber' Role has commented Channel 'id-hubber' and in {guildid} or not

Notice: The {handler} must be in the URL-Encode format.

Request: GET /discord/commented/idhubber?handler={}&guildid={}

Response:

```json
{
    "data": true,
    "message": "Success",
    "hasErrors": false,
    "msgCode": 100000,
    "success": true
}
```

## Twitter

### Get User Id By Handler

Request: GET /twitter/uid?handler={}

Response:

```json
{
 "data": {
  "id": "3262922634",
  "name": "name1",
  "handler": "handler1"
 },
 "message": "Success",
 "hasErrors": false,
 "msgCode": 100000,
 "success": true
}
```

### Get Tweet By Id

Request: GET /twitter/tweet?tid={}

Response:

```json
{
 "data": {
  "id": "1493152933760110592",
  "authorId": "3262922634",
  "createdAt": "2022-02-14T09:19:35Z",
  "text": "Witnessing User ID did:mcp:0xc8870ca4a8e3f581821384f3c927a9494a661b35:evm proving its ownership for Twitter account on https://t.co/JZhtEIXif8, challenge code is: 8ca03b4015155c8be26ba49dc8d2db54."
 },
 "message": "Success",
 "hasErrors": false,
 "msgCode": 100000,
 "success": true
}
```

### Twitter Followers Verification

Check whether the {handler2} is following {handler1} or not.

Due to the twitter api limitations, `only the first 1000 followers` can be fetched.

Will improve this to multiple "pages" of results in the future version.

Request: GET /twitter/followers/verification?handler1={handler1}&handler2={handler2}

Response:

```json
{
 "data": true,
 "message": "Success",
 "hasErrors": false,
 "msgCode": 100000,
 "success": true
}
```

## Success or Error codes

### Common codes

- 99000  Fail
- 100000 Success

### Discord codes

- 100101 Discord verify message found
- 100102 Discord verify message not found
- 100103 Discord guild id invalid
- 100104 Discord user handler invalid
- 100105 Discord user is not in guild

### Twitter codes

- 100201 Twitter handler not found
