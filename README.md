# TEE-bot
microservice within TEE

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
```
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

## Twitter

### Twitter Followers Verification
TBD

## Error codes
-  99000  Fail
- 100000 Success
- 100101 Discord verify message found
- 100102 Discord verify message not found